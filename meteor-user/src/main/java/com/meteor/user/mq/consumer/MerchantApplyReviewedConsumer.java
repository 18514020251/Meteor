package com.meteor.user.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.mq.contract.enums.merchant.MerchantApplyStatus; // ✅ contract enum
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import com.meteor.satoken.context.LoginContext;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.domain.entity.User;
import com.meteor.user.enums.RoleEnum;
import com.meteor.user.mapper.MerchantApplyMapper;
import com.meteor.user.mapper.UserMapper;
import com.meteor.user.service.cache.IUserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商家申请审核结果消息队列消费者
 *
 * @author Programmer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantApplyReviewedConsumer {

    private final MerchantApplyMapper merchantApplyMapper;
    private final LoginContext loginContext;
    private final IUserCacheService userCacheService;
    private final UserMapper userMapper;

    @RabbitListener(queues = MerchantApplyContract.Queue.MERCHANT_APPLY_REVIEWED, errorHandler = "mqRejectErrorHandler")
    @Transactional(rollbackFor = Exception.class)
    public void handle(MerchantApplyReviewedMessage message) {
        validate(message);

        MerchantApplyStatus contractStatus = message.getStatus();

        MerchantApplyStatusEnum statusEnum = MerchantApplyStatusEnum.valueOf(contractStatus.name());

        LambdaUpdateWrapper<MerchantApply> updateWrapper = buildUpdateWrapper(message, statusEnum);

        int rows = merchantApplyMapper.update(null, updateWrapper);

        if (rows > 0) {
            followUpActions(message);
            log.info("商家申请审核状态更新成功，申请ID={}, 新状态={}", message.getApplyId(), statusEnum);
            return;
        }

        MerchantApply apply = merchantApplyMapper.selectById(message.getApplyId());

        if (apply == null) {
            log.error("商家申请记录不存在，申请ID={}", message.getApplyId());
            return;
        }

        if (!MerchantApplyStatusEnum.PENDING.getCode().equals(apply.getStatus())) {
            log.info("商家申请已被处理，无需重复更新，申请ID={}, 当前状态={}, msgStatus={}",
                    message.getApplyId(), apply.getStatus(), statusEnum);
            return;
        }

        log.error("商家申请审核状态更新失败：rows==0 但 DB 仍为PENDING，申请ID={}, dbStatus={}, msgStatus={}",
                message.getApplyId(), apply.getStatus(), statusEnum);
    }

    /**
     * 参数校验
     */
    private void validate(MerchantApplyReviewedMessage message) {
        if (message == null || message.getApplyId() == null || message.getStatus() == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }

        if (message.getStatus() == MerchantApplyStatus.APPROVED && message.getUserId() == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }
    }

    /**
     * 构建修改条件
     */
    private LambdaUpdateWrapper<MerchantApply> buildUpdateWrapper(MerchantApplyReviewedMessage message,  MerchantApplyStatusEnum statusEnum) {
        return new LambdaUpdateWrapper<MerchantApply>()
                .eq(MerchantApply::getId, message.getApplyId())
                .eq(MerchantApply::getStatus, MerchantApplyStatusEnum.PENDING.getCode())
                .set(MerchantApply::getStatus, statusEnum.getCode());
    }


    /**
     * <p>后续处理</p>
     * @param message 商家申请审核结果消息
     * */
    private void followUpActions(MerchantApplyReviewedMessage message) {
        if (message.getStatus() != MerchantApplyStatus.APPROVED) {
            return;
        }

        Long userId = message.getUserId();
        if (userId == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }

        int updated = userMapper.update(null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getId, userId)
                        .ne(User::getRole, RoleEnum.MERCHANT.getCode())
                        .set(User::getRole, RoleEnum.MERCHANT.getCode())
        );

        if (updated == 0) {
            User u = userMapper.selectById(userId);
            if (u == null) {
                throw new BizException(CommonErrorCode.DATA_ERROR);
            }
        }

        try {
            userCacheService.evictUserAll(userId);
            loginContext.kickout(userId);
        } catch (Exception e) {
            log.warn("followUpActions non-critical failed, userId={}", userId, e);
        }
    }

}

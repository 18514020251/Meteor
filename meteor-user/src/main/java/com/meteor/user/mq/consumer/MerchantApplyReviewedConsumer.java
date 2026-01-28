package com.meteor.user.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.mq.merchant.MerchantApplyReviewedMessage;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.satoken.context.LoginContext;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.mapper.MerchantApplyMapper;
import com.meteor.user.service.cache.IUserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *  商家申请审核结果消息队列消费者
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

    @RabbitListener(queues = MerchantApplyContract.Queue.MERCHANT_APPLY_REVIEWED , errorHandler = "mqRejectErrorHandler")
    @Transactional(rollbackFor = Exception.class)
    public void handle(MerchantApplyReviewedMessage message) {

        validate(message);

        MerchantApplyStatusEnum statusEnum = message.getStatus();

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
            log.info("商家申请已被处理，无需重复更新，申请ID={}, 当前状态={}", message.getApplyId(), apply.getStatus());
            return;
        }

        log.error("商家申请审核状态更新失败，数据库状态异常，申请ID={}, msgStatus={}", message.getApplyId(), statusEnum);
    }

    /**
     * <p>参数校验</p>
     * @param message 商家申请审核结果消息
     * */
    private void validate(MerchantApplyReviewedMessage message){
        if (message == null || message.getApplyId() == null || message.getStatus() == null || message.getUserId() == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }
    }

    /**
     * <p>构建修改条件</p>
     * @param message 商家申请审核结果消息
     * @param statusEnum 状态枚举
     * */
    private LambdaUpdateWrapper<MerchantApply> buildUpdateWrapper(MerchantApplyReviewedMessage message , MerchantApplyStatusEnum statusEnum) {
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
        if (message.getStatus() != MerchantApplyStatusEnum.APPROVED) {
            return;
        }
        Long userId = message.getUserId();

        try {
            userCacheService.evictUserAll(userId);
            loginContext.kickout(userId);
        } catch (Exception e) {
            log.warn("followUpActions failed, userId={}", userId, e);
        }
    }

}

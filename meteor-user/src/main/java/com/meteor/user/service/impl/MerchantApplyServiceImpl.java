package com.meteor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.satoken.context.LoginContext;
import com.meteor.user.controller.dto.MerchantApplyDTO;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.domain.entity.User;
import com.meteor.user.mapper.MerchantApplyMapper;
import com.meteor.user.mapper.UserMapper;
import com.meteor.user.mq.publisher.MerchantApplyEventPublisher;
import com.meteor.user.mq.publisher.MessageApplyEventPublisher;
import com.meteor.user.service.IMerchantApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.meteor.common.exception.CommonErrorCode.OPERATION_NOT_ALLOWED;

/**
 *  用户申请成为商家服务实现类
 *
 * @author Programmer
 * @date 2026-01-20 23:36
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantApplyServiceImpl extends ServiceImpl<UserMapper, User>  implements IMerchantApplyService{

    private final MerchantApplyMapper merchantApplyMapper;
    private final MerchantApplyEventPublisher eventPublisher;
    private final LoginContext loginContext;
    private final MessageApplyEventPublisher messageApplyEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(Long userId, MerchantApplyDTO dto) {
        loginContext.checkUserRole();

        MerchantApply apply = createAndPersistApply(userId, dto);

        publishCreatedEvent(apply);

        try {
            messageApplyEventPublisher.publishMerchantApplySubmitted(
                    userId,
                    apply.getId(),
                    apply.getShopName()
            );
        } catch (Exception e) {
            log.warn("publishMerchantApplySubmitted failed, userId={}, applyId={}, err={}",
                    userId, apply.getId(), e, e);
        }
    }

    private MerchantApply createAndPersistApply(Long userId, MerchantApplyDTO dto) {
        boolean exists = merchantApplyMapper.exists(
                new LambdaQueryWrapper<MerchantApply>()
                        .eq(MerchantApply::getUserId, userId)
                        .in(MerchantApply::getStatus,
                                MerchantApplyStatusEnum.PENDING,
                                MerchantApplyStatusEnum.APPROVED)
        );

        if (exists) {
            throw new BizException(OPERATION_NOT_ALLOWED);
        }

        MerchantApply apply = MerchantApply.pending(
                userId,
                dto.getShopName(),
                dto.getApplyReason()
        );

        merchantApplyMapper.insert(apply);
        return apply;
    }


    private void publishCreatedEvent(MerchantApply apply) {
        eventPublisher.publishCreatedOrThrow(apply);
    }
}



package com.meteor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.common.exception.BizException;
import com.meteor.user.domain.dto.MerchantApplyDTO;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.user.mapper.MerchantApplyMapper;
import com.meteor.user.mq.publisher.MerchantApplyEventPublisher;
import com.meteor.user.service.IMerchantApplyService;
import lombok.RequiredArgsConstructor;
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
public class MerchantApplyServiceImpl implements IMerchantApplyService {

    private final MerchantApplyMapper merchantApplyMapper;
    private final MerchantApplyEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(Long userId, MerchantApplyDTO applyReason) {

        boolean exists = merchantApplyMapper.exists(
                new LambdaQueryWrapper<MerchantApply>()
                        .eq(MerchantApply::getUserId, userId)
                        .in(MerchantApply::getStatus,
                                MerchantApplyStatusEnum.PENDING.getCode(),
                                MerchantApplyStatusEnum.APPROVED.getCode())
        );

        if (exists) {
            throw new BizException(OPERATION_NOT_ALLOWED);
        }

        MerchantApply apply = MerchantApply.pending(
                userId,
                applyReason.getShopName(),
                applyReason.getApplyReason()
        );

        merchantApplyMapper.insert(apply);

        eventPublisher.publishCreatedOrThrow(apply);
    }

}


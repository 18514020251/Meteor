package com.meteor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.common.exception.BizException;
import com.meteor.mq.constant.MqConstants;
import com.meteor.mq.core.MqSender;
import com.meteor.user.domain.dto.MerchantApplyDTO;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.domain.event.MerchantApplyCreatedEvent;
import com.meteor.user.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.user.mapper.MerchantApplyMapper;
import com.meteor.user.service.IMerchantApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final MqSender mqSender;

    @Override
    public void apply(Long userId, MerchantApplyDTO applyReason) {

        boolean exists = merchantApplyMapper.exists(
                new LambdaQueryWrapper<MerchantApply>()
                        .eq(MerchantApply::getUserId, userId)
                        .in(MerchantApply::getStatus,
                                MerchantApplyStatusEnum.PENDING.getCode(),
                                MerchantApplyStatusEnum.APPROVED.getCode()
                        )
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

        MerchantApplyCreatedEvent event = MerchantApplyCreatedEvent.of(apply);

        mqSender.send(
                MqConstants.Exchange.MERCHANT_APPLY,
                MqConstants.RoutingKey.MERCHANT_APPLY_CREATED,
                event
        );


    }
}


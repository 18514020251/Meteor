package com.meteor.admin.service.impl;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.mq.assembler.MerchantApplyAssembler;
import com.meteor.admin.service.IMerchantApplyReviewedService;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.mq.merchant.MerchantApplyReviewedMessage;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Programmer
 * @date 2026-01-27 18:07
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantApplyReviewedServiceImpl implements IMerchantApplyReviewedService {

    private final MerchantApplyAssembler merchantApplyAssembler;
    private final MqSender mqSender;

    @Override
    public void send(MerchantApply apply) {
        MerchantApplyReviewedMessage message = merchantApplyAssembler.toReviewedMessage(apply);
        MqSendResult result = mqSender.sendAndWaitConfirm(
                MerchantApplyContract.Exchange.MERCHANT_APPLY,
                MerchantApplyContract.RoutingKey.MERCHANT_APPLY_REVIEWED,
                message,
                MerchantApplyContract.CONFIRM_TIMEOUT
        );

        if (!result.isAck()) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR,
                    "MQ confirm failed");
        }

        if (result.noRoute()) {
            log.warn("MQ NO_ROUTE");
        }
    }
}

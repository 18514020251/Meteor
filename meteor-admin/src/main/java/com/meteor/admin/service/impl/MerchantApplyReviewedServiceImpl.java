package com.meteor.admin.service.impl;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.mq.mapper.MerchantApplyMqAssemblerMapper;
import com.meteor.admin.service.IMerchantApplyReviewedService;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 *  发送商家申请审核结果
 *  MQ 发送
 *
 * @author Programmer
 * @date 2026-01-27 18:07
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantApplyReviewedServiceImpl implements IMerchantApplyReviewedService {

    private final MerchantApplyMqAssemblerMapper merchantApplyAssembler;
    private final MqSender mqSender;

    @Override
    public void send(MerchantApply apply) {
        doSend(apply);
    }

    @Override
    public void send(MerchantApply apply, Runnable onSuccess) {

        MqSendResult result = doSend(apply);
        if (result == null) {
            return;
        }

        if (onSuccess != null) {
            try {
                onSuccess.run();
            } catch (Exception e) {
                log.warn("MQ sent ok but onSuccess failed, applyId={}", apply.getApplyId(), e);
                throw new BizException(CommonErrorCode.SYSTEM_ERROR);
            }
        }
    }

    /**
     * 发送逻辑
     */
    private MqSendResult doSend(MerchantApply apply) {

        MerchantApplyReviewedMessage message = merchantApplyAssembler.toReviewedMessage(apply);

        MqSendResult result = mqSender.sendAndWaitConfirm(
                MerchantApplyContract.Exchange.MERCHANT_APPLY,
                MerchantApplyContract.RoutingKey.MERCHANT_APPLY_REVIEWED,
                message,
                MerchantApplyContract.CONFIRM_TIMEOUT
        );

        if (result == null) {
            log.error("MQ sendAndWaitConfirm returned null, applyId={}", apply.getApplyId());
            return MqSendResult.failed();
        }

        if (!result.isAck()) {
            log.error("MQ confirm failed, applyId={}, exchange={}, routingKey={}",
                    apply.getApplyId(),
                    MerchantApplyContract.Exchange.MERCHANT_APPLY,
                    MerchantApplyContract.RoutingKey.MERCHANT_APPLY_REVIEWED);
            return result;
        }

        if (result.noRoute()) {
            log.warn("MQ NO_ROUTE, applyId={}, exchange={}, routingKey={}",
                    apply.getApplyId(),
                    MerchantApplyContract.Exchange.MERCHANT_APPLY,
                    MerchantApplyContract.RoutingKey.MERCHANT_APPLY_REVIEWED);
            return result;
        }

        return result;
    }
}
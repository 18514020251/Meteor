package com.meteor.merchant.mq.consumer;

import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.merchant.service.tx.MerchantActivatedTxService;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import com.meteor.mq.contract.merchant.MerchantContract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 *  商家申请审核通过消息队列消费者
 *
 * @author Programmer
 * @date 2026-01-31 23:01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantActivatedConsumer {

    private final MerchantActivatedTxService txService;

    @RabbitListener(
            queues = MerchantContract.Queue.MERCHANT_ACTIVATED,
            errorHandler = "mqRejectErrorHandler"
    )
    public void handle(MerchantApplyReviewedMessage message) {
        validate(message);

        if (!MerchantApplyStatusEnum.APPROVED.getCode().equals(message.getStatusCode())) {
            log.info("ignore non-approved merchant activation message, applyId={}, statusCode={}",
                    message.getApplyId(), message.getStatusCode());
            return;
        }

        txService.processApproved(message);
    }

    private void validate(MerchantApplyReviewedMessage message) {
        if (message == null
                || message.getApplyId() == null
                || message.getStatusCode() == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }

        if (MerchantApplyStatusEnum.APPROVED.getCode().equals(message.getStatusCode())) {
            if (message.getUserId() == null) {
                throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
            }
            if (message.getShopName() == null || message.getShopName().isBlank()) {
                throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
            }
        }
    }
}

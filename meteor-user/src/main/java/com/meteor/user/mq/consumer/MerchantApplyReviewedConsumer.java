package com.meteor.user.mq.consumer;

import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import com.meteor.user.service.tx.MerchantApplyReviewedTxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 商家申请审核结果消息队列消费者
 * @author Programmer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantApplyReviewedConsumer {

    private final MerchantApplyReviewedTxService txService;

    @RabbitListener(
            queues = MerchantApplyContract.Queue.MERCHANT_APPLY_REVIEWED,
            errorHandler = "mqRejectErrorHandler"
    )
    public void handle(MerchantApplyReviewedMessage message) {
        validate(message);

        var result = txService.process(message);

        if (result.isApproved()) {
            log.info("商家申请审核通过，准备执行后续操作，applyId={}, userId={}",
                    message.getApplyId(), message.getUserId());
        }
    }

    /**
     * 参数校验
     */
    private void validate(MerchantApplyReviewedMessage message) {
        if (message == null || message.getApplyId() == null || message.getStatusCode() == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }
        if (message.getStatusCode().equals(MerchantApplyStatusEnum.APPROVED.getCode())
                && message.getUserId() == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }
        if (message.getStatusCode().equals(MerchantApplyStatusEnum.APPROVED.getCode())
                && (message.getShopName() == null || message.getShopName().isBlank())) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }
    }
}

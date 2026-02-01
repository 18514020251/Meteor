package com.meteor.user.mq.consumer;

import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import com.meteor.mq.contract.merchant.UserDeactivatedMessage;
import com.meteor.user.service.tx.MerchantApplyReviewedTxService;
import com.meteor.user.service.tx.model.TxResult;
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

        TxResult result = txService.process(message);

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
        boolean approved = MerchantApplyStatusEnum.APPROVED.getCode()
                .equals(message.getStatusCode());

        boolean shopNameInvalid =
                message.getShopName() == null || message.getShopName().isBlank();

        if (approved && shopNameInvalid) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }
    }

    /**
     * 校验 UserDeactivatedMessage 参数是否合法
     */
    private void validate(UserDeactivatedMessage message) {
        if (message == null || message.getUserId() == null || message.getTimestamp() == null) {
            throw new IllegalArgumentException("UserDeactivatedMessage invalid: " + message);
        }
    }

    @RabbitListener(
            queues = MerchantApplyContract.Queue.USER_DEACTIVATED,
            errorHandler = "mqRejectErrorHandler"
    )
    public void handle(UserDeactivatedMessage message) {
        // 校验消息
        validate(message);

        TxResult result = txService.processDeactivation(message);

        if (result.isUpdated()) {
            log.info("收到商家注销事件，已更新用户角色为普通用户，userId={}, ts={}",
                    message.getUserId(), message.getTimestamp());
        } else {
            log.info("商家注销事件已处理，无需更新用户角色，userId={}, ts={}",
                    message.getUserId(), message.getTimestamp());
        }
    }
}

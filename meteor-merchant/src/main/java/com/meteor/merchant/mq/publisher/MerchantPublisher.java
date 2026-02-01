package com.meteor.merchant.mq.publisher;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.UserDeactivatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;


/**
 * 商家相关的 MQ 发布实现
 *  @author Programmer
 *  @date 2026-02-01 15:59
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MerchantPublisher {

    private final AmqpTemplate amqpTemplate;

    public void publishUserDeactivatedEvent(UserDeactivatedMessage msg) {
        if (msg == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "msg is null");
        }
        if (msg.getUserId() == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "userId is null");
        }
        if (msg.getTimestamp() == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "timestamp is null");
        }

        try {
            amqpTemplate.convertAndSend(
                    MerchantApplyContract.Exchange.USER_EVENT,
                    MerchantApplyContract.RoutingKey.USER_DEACTIVATED,
                    msg
            );

            log.debug("MQ sent: user.deactivated, userId={}, ts={}",
                    msg.getUserId(), msg.getTimestamp());

        } catch (Exception ex) {
            log.error("MQ send failed: exchange={}, routingKey={}, userId={}, ts={}",
                    MerchantApplyContract.Exchange.USER_EVENT,
                    MerchantApplyContract.RoutingKey.USER_DEACTIVATED,
                    msg.getUserId(),
                    msg.getTimestamp(),
                    ex
            );

            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MQ send failed");
        }
    }
}
package com.meteor.order.mq.publisher;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.order.OrderPayTimeoutContract;
import com.meteor.mq.contract.order.OrderPayTimeoutMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 订单支付超时闹钟发布者（发送到延时队列）
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 19:17
 */
@Component
@RequiredArgsConstructor
public class OrderPayTimeoutPublisher {

    private final MqSender mqSender;

    public void publishOrThrow(OrderPayTimeoutMessage message) {

        MqSendResult result = mqSender.sendAndWaitConfirm(
                OrderPayTimeoutContract.Exchange.ORDER,
                OrderPayTimeoutContract.RoutingKey.ORDER_PAY_TIMEOUT_DELAY,
                message,
                OrderPayTimeoutContract.CONFIRM_TIMEOUT
        );

        if (!result.isAck()) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MQ confirm failed");
        }
        if (result.noRoute()) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MQ NO_ROUTE");
        }
    }
}

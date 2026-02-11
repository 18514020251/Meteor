package com.meteor.order.mq.publisher;

import com.meteor.mq.contract.analytics.OperationAnalyticsContract;
import com.meteor.mq.contract.analytics.PayCreatedMessage;
import com.meteor.mq.contract.analytics.PaySuccessMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 运营分析发布者
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 19:07
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationAnalyticsPublisher {

    private final MqSender mqSender;

    public void publishPayCreated(PayCreatedMessage message) {
        doSend(
                OperationAnalyticsContract.RoutingKey.PAY_CREATED,
                message,
                message == null ? null : message.getEventId()
        );
    }

    public void publishPaySuccess(PaySuccessMessage message) {
        doSend(
                OperationAnalyticsContract.RoutingKey.PAY_SUCCESS,
                message,
                message == null ? null : message.getEventId()
        );
    }

    private void doSend(String routingKey, Object payload, String eventId) {

        MqSendResult result = mqSender.sendAndWaitConfirm(
                OperationAnalyticsContract.Exchange.ANALYTICS,
                routingKey,
                payload,
                OperationAnalyticsContract.CONFIRM_TIMEOUT
        );

        if (!result.isAck()) {
            log.error("MQ confirm failed: exchange={}, routingKey={}, eventId={}",
                    OperationAnalyticsContract.Exchange.ANALYTICS, routingKey, eventId);
            return;
        }

        if (result.noRoute()) {
            log.warn("MQ NO_ROUTE: exchange={}, routingKey={}, eventId={}",
                    OperationAnalyticsContract.Exchange.ANALYTICS, routingKey, eventId);
        }
    }
}

package com.meteor.user.mq.publisher;

import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.analytics.OperationAnalyticsContract;
import com.meteor.mq.contract.analytics.UserRegisteredMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import com.meteor.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 运营分析事件发布器（用户注册）
 *
 * @author Programmer
 * @date 2026-02-11
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationAnalyticsEventPublisher {

    private final MqSender mqSender;
    private final SnowflakeIdGenerator idGenerator;

    public void publishUserRegistered(User user) {
        if (user == null || user.getId() == null) {
            log.warn("skip publish user.registered: user or userId is null");
            return;
        }

        String eventId = "ur:" + idGenerator.nextId();

        UserRegisteredMessage message = new UserRegisteredMessage(
                eventId,
                user.getId(),
                LocalDateTime.now()
        );

        doSend(message);
    }

    private void doSend(UserRegisteredMessage message) {
        MqSendResult result = mqSender.sendAndWaitConfirm(
                OperationAnalyticsContract.Exchange.ANALYTICS,
                OperationAnalyticsContract.RoutingKey.USER_REGISTERED,
                message,
                OperationAnalyticsContract.CONFIRM_TIMEOUT
        );

        if (!result.isAck()) {
            log.error("MQ confirm failed: exchange={}, routingKey={}, eventId={}, cause={}",
                    OperationAnalyticsContract.Exchange.ANALYTICS,
                    OperationAnalyticsContract.RoutingKey.USER_REGISTERED,
                    message.getEventId(),
                    result.getCause());
            return;
        }

        if (result.noRoute()) {
            log.warn("MQ NO_ROUTE: exchange={}, routingKey={}, eventId={}",
                    OperationAnalyticsContract.Exchange.ANALYTICS,
                    OperationAnalyticsContract.RoutingKey.USER_REGISTERED,
                    message.getEventId());
        }
    }
}

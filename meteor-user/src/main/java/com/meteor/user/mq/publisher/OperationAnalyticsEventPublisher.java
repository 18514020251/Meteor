package com.meteor.user.mq.publisher;

import com.meteor.common.enums.system.ModuleEnum;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.analytics.OperationAnalyticsContract;
import com.meteor.mq.contract.analytics.UserRegisteredMessage;
import com.meteor.mq.core.MqSender;
import com.meteor.user.domain.cmd.MqSendCmd;
import com.meteor.user.domain.entity.User;
import com.meteor.user.mq.support.UserMqSendGuard;
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
    private final UserMqSendGuard mqGuard;

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

        mqGuard.send(new MqSendCmd(
                message.getEventId(),
                ModuleEnum.USER,
                message.getUserId(),
                OperationAnalyticsContract.Exchange.ANALYTICS,
                OperationAnalyticsContract.RoutingKey.USER_REGISTERED,
                "user_registered",
                message,
                OperationAnalyticsContract.CONFIRM_TIMEOUT,
                false
        ));
    }
}

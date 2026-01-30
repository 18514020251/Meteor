package com.meteor.user.mq.publisher;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.enums.message.UserEventType;
import com.meteor.mq.contract.message.UserEventMessage;
import com.meteor.mq.contract.message.UserMessageContract;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import com.meteor.user.domain.entity.User;
import com.meteor.user.mq.assembler.MessageApplyMessageAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *  消息发布器
 *
 * @author Programmer
 * @date 2026-01-30 12:41
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageApplyEventPublisher {
    private final MqSender mqSender;
    private final MessageApplyMessageAssembler assembler;

    public void publishPasswordChanged(User user) {
        UserEventMessage message = assembler.from(user , UserEventType.USER_PASSWORD_CHANGED);

        MqSendResult result = mqSender.sendAndWaitConfirm(
                UserMessageContract.Exchange.USER_MESSAGE,
                UserMessageContract.RoutingKey.USER_MESSAGE_CREATED,
                message,
                UserMessageContract.CONFIRM_TIMEOUT
        );

        if (!result.isAck()) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MQ confirm failed");
        }

        if (result.noRoute()) {
             log.warn("MQ NO_ROUTE: exchange={}, routingKey={}, eventId={}",
                     UserMessageContract.Exchange.USER_MESSAGE,
                     UserMessageContract.RoutingKey.USER_MESSAGE_CREATED,
                     message.getEventId());
        }
    }
}

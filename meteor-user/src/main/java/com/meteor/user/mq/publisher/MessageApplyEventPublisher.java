package com.meteor.user.mq.publisher;

import com.meteor.mq.contract.enums.message.UserEventType;
import com.meteor.mq.contract.message.UserEventMessage;
import com.meteor.mq.contract.message.UserMessageContract;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import com.meteor.user.domain.entity.User;
import com.meteor.user.mq.assembler.MessageUserEventMessageAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    private final MessageUserEventMessageAssembler assembler;

    public void publishPasswordChanged(User user) {
        UserEventMessage message = assembler.from(user, UserEventType.USER_PASSWORD_CHANGED);
        doSend(message);
    }

    /**
     * 用户提交商家申请
     */
    public void publishMerchantApplySubmitted(Long userId, Long applyId, String shopName) {
        UserEventMessage message = assembler.from(userId, UserEventType.MERCHANT_APPLY_SUBMITTED);

        message.setBizId(String.valueOf(applyId));

        message.setPayload(Map.of(
                "applyId", String.valueOf(applyId),
                "shopName", shopName == null ? "" : shopName
        ));

        doSend(message);
    }

    private void doSend(UserEventMessage message) {
        MqSendResult result = mqSender.sendAndWaitConfirm(
                UserMessageContract.Exchange.USER_MESSAGE,
                UserMessageContract.RoutingKey.USER_MESSAGE_CREATED,
                message,
                UserMessageContract.CONFIRM_TIMEOUT
        );

        if (!result.isAck()) {
            log.error("MQ confirm failed: exchange={}, routingKey={}, eventId={}",
                    UserMessageContract.Exchange.USER_MESSAGE,
                    UserMessageContract.RoutingKey.USER_MESSAGE_CREATED,
                    message.getEventId());
            return;
        }

        if (result.noRoute()) {
            log.warn("MQ NO_ROUTE: exchange={}, routingKey={}, eventId={}",
                    UserMessageContract.Exchange.USER_MESSAGE,
                    UserMessageContract.RoutingKey.USER_MESSAGE_CREATED,
                    message.getEventId());
        }
    }
}

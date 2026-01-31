package com.meteor.user.mq.assembler;

import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.enums.message.UserEventType;
import com.meteor.mq.contract.message.UserEventMessage;
import com.meteor.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 *  消息 MQ消息构造器
 *
 * @author Programmer
 * @date 2026-01-30 16:15
 */
@Component
@RequiredArgsConstructor
public class MessageUserEventMessageAssembler {

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public UserEventMessage from(User user, UserEventType eventType) {
        return from(user.getId(), eventType);
    }

    public UserEventMessage from(Long userId, UserEventType eventType) {
        UserEventMessage msg = new UserEventMessage();
        msg.setEventId(snowflakeIdGenerator.nextId());
        msg.setEventType(eventType.getCode());
        msg.setUserId(userId);
        msg.setOccurredAt(LocalDateTime.now());
        msg.setPayload(new HashMap<>(16));
        return msg;
    }
}

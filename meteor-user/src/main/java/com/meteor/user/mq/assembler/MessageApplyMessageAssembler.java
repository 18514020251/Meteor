package com.meteor.user.mq.assembler;

import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.enums.message.UserEventType;
import com.meteor.mq.contract.message.UserEventMessage;
import com.meteor.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *  消息 MQ消息构造器
 *
 * @author Programmer
 * @date 2026-01-30 16:15
 */
@Component
@RequiredArgsConstructor
public class MessageApplyMessageAssembler {

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public UserEventMessage from(User user, UserEventType eventType) {
        UserEventMessage msg = new UserEventMessage();

        msg.setEventId(snowflakeIdGenerator.nextId());

        msg.setEventType(eventType.getCode());

        msg.setUserId(user.getId());

        msg.setOccurredAt(LocalDateTime.now());

        return msg;
    }
}

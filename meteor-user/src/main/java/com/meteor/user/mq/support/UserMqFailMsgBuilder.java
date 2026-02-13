package com.meteor.user.mq.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.meteor.api.model.UserMqFailureEntity;
import com.meteor.common.constants.MqConstants;
import com.meteor.common.enums.system.mq.MessageStatusEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *  创建失败消息
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 14:57
 */
@Component
public class UserMqFailMsgBuilder {

    private final ObjectMapper objectMapper;

    public UserMqFailMsgBuilder() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public UserMqFailureEntity build(UserMqFailBuildCmd cmd) {

        try {
            LocalDateTime now = LocalDateTime.now();

            UserMqFailureEntity msg = new UserMqFailureEntity();
            msg.setMsgId(cmd.getMsgId());
            msg.setModuleName(cmd.getModule());
            msg.setBizId(cmd.getBizId());
            msg.setExchangeName(cmd.getExchange());
            msg.setRoutingKey(cmd.getRoutingKey());
            msg.setTopic(cmd.getTopic());
            msg.setPayload(objectMapper.writeValueAsString(cmd.getPayload()));
            msg.setStatus(MessageStatusEnum.PENDING);
            msg.setRetryCnt(MqConstants.DEFAULT_RETRY_COUNT);
            msg.setLastError(cmd.getErrorMsg());
            msg.setNextRetryTime(now.plusMinutes(MqConstants.DEFAULT_NEXT_RETRY_TIME));
            msg.setCreateTime(now);
            msg.setUpdateTime(now);

            return msg;

        } catch (Exception e) {
            throw new IllegalStateException("Build UserMqFailMsg failed", e);
        }
    }
}

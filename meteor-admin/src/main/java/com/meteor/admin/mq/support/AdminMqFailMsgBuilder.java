package com.meteor.admin.mq.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.api.model.AdminMqFailureEntity;
import com.meteor.common.constants.MqConstants;
import com.meteor.common.enums.system.ModuleEnum;
import com.meteor.common.enums.system.mq.MessageStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MQ发送失败消息构建器
 *
 * @author Programmer
 * @version 1.1
 * @date 2026-02-12
 */
@Component
@RequiredArgsConstructor
public class AdminMqFailMsgBuilder {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AdminMqFailMsgBuilder(ObjectMapper objectMapper) {
        this(objectMapper, Clock.systemDefaultZone());
    }

    public AdminMqFailureEntity build(BuildCmd cmd) {
        Objects.requireNonNull(cmd, "cmd must not be null");

        String payloadJson = toJson(cmd.payload());
        LocalDateTime now = LocalDateTime.now(clock);

        AdminMqFailureEntity failMsg = new AdminMqFailureEntity();
        failMsg.setMsgId(cmd.msgId());
        failMsg.setModuleName(cmd.module());
        failMsg.setBizId(cmd.bizId());
        failMsg.setExchangeName(cmd.exchange());
        failMsg.setRoutingKey(cmd.routingKey());
        failMsg.setTopic(cmd.topic());
        failMsg.setPayload(payloadJson);
        failMsg.setStatus(MessageStatusEnum.PENDING);
        failMsg.setRetryCnt(MqConstants.DEFAULT_RETRY_COUNT);
        failMsg.setLastError(cmd.errorMsg());
        failMsg.setNextRetryTime(now.plusMinutes(MqConstants.DEFAULT_NEXT_RETRY_TIME));
        failMsg.setCreateTime(now);
        failMsg.setUpdateTime(now);

        return failMsg;
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Serialize MQ payload failed", e);
        }
    }

    /**
     * 参数对象
     */
    public record BuildCmd(
            String msgId,
            ModuleEnum module,
            Long bizId,
            String exchange,
            String routingKey,
            String topic,
            Object payload,
            String errorMsg
    ) {}
}

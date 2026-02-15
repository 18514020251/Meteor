package com.meteor.ticketing.mq.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.common.constants.MqConstants;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.enums.OutboxStatus;
import com.meteor.ticketing.service.IMqOutboxEventService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *  MqOutbox事件写入器，用于将事件写入MqOutbox表
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-15 14:28
 */
@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final IMqOutboxEventService outboxService;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Data
    @Builder
    public static class SaveEventParams {
        private String bizKey;
        private String eventType;
        private String exchange;
        private String routingKey;
        private Object message;
        private String traceId;
        private LocalDateTime deliverAt;
        private LocalDateTime bizExpireAt;
    }

    public void saveEvent(SaveEventParams params) {
        try {
            validate(params);
            String payload = objectMapper.writeValueAsString(params.getMessage());
            // NOTE:后续提取为Assembler或全参
            MqOutboxEvent e = new MqOutboxEvent()
                    .setId(idGenerator.nextId())
                    .setBizKey(params.getBizKey())
                    .setEventType(params.getEventType())
                    .setExchangeName(params.getExchange())
                    .setRoutingKey(params.getRoutingKey())
                    .setPayload(payload)
                    .setStatus(OutboxStatus.NEW)
                    .setRetryCnt(MqConstants.DEFAULT_RETRY_COUNT)
                    .setNextRetryTime(LocalDateTime.now())
                    .setDeliverAt(params.getDeliverAt())
                    .setBizExpireAt(params.getBizExpireAt())
                    .setTraceId(params.getTraceId());

            if (!outboxService.save(e)) {
                throw new BizException(CommonErrorCode.SYSTEM_ERROR,
                        String.format("保存MqOutboxEvent失败: bizKey=%s, eventType=%s",
                                params.getBizKey(), params.getEventType()));
            }
        } catch (JsonProcessingException ex) {
            throw new BizException(CommonErrorCode.PARAM_ERROR,
                    "消息体JSON序列化失败: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new BizException(CommonErrorCode.PARAM_ERROR,
                    "参数校验失败: " + ex.getMessage());
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR,
                    "保存Outbox事件时发生未知错误: " + ex.getMessage());
        }
    }

    private void validate(SaveEventParams p) {
        if (p == null) throw new IllegalArgumentException("params is null");
        if (p.getBizKey() == null || p.getBizKey().isBlank()) throw new IllegalArgumentException("bizKey blank");
        if (p.getEventType() == null || p.getEventType().isBlank()) throw new IllegalArgumentException("eventType blank");
        if (p.getExchange() == null || p.getExchange().isBlank()) throw new IllegalArgumentException("exchange blank");
        if (p.getRoutingKey() == null || p.getRoutingKey().isBlank()) throw new IllegalArgumentException("routingKey blank");
        if (p.getMessage() == null) throw new IllegalArgumentException("message is null");
        if (p.getDeliverAt() == null) throw new IllegalArgumentException("deliverAt is null");
        if (p.getBizExpireAt() == null) throw new IllegalArgumentException("bizExpireAt is null");
    }

}

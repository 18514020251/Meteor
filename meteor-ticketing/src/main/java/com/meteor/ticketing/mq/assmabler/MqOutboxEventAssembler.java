package com.meteor.ticketing.mq.assmabler;

import com.meteor.common.constants.MqConstants;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.ticketing.TicketOrderContract;
import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.enums.OutboxStatus;
import com.meteor.ticketing.mq.outbox.OutboxWriter;
import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-15 15:55
 */
@Component
@RequiredArgsConstructor
public class MqOutboxEventAssembler {

    private final SnowflakeIdGenerator idGenerator;
    private static final String EVT_TICKET_ORDER_CREATE = "TICKET_ORDER_CREATE";
    private static final String EVT_TICKET_DB_RESERVED = "TICKET_DB_RESERVED";

    public MqOutboxEvent from(OutboxWriter.SaveEventParams params, String payload) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime nextRetry = now;
        if (params.getDeliverAt() != null && params.getDeliverAt().isAfter(now)) {
            nextRetry = params.getDeliverAt();
        }

        return new MqOutboxEvent()
                .setId(idGenerator.nextId())
                .setBizKey(params.getBizKey())
                .setEventType(params.getEventType())
                .setExchangeName(params.getExchange())
                .setRoutingKey(params.getRoutingKey())
                .setPayload(payload)
                .setStatus(OutboxStatus.NEW)
                .setRetryCnt(MqConstants.DEFAULT_RETRY_COUNT)
                .setNextRetryTime(nextRetry)
                .setDeliverAt(params.getDeliverAt())
                .setBizExpireAt(params.getBizExpireAt())
                .setTraceId(params.getTraceId());
    }

    /**
     * 构建TicketOrderCreate事件
     */
    public MqOutboxEvent buildTicketOrderCreate(String orderNo,
                                                String payload,
                                                LocalDateTime deliverAt,
                                                LocalDateTime bizExpireAt) {
        return new MqOutboxEvent()
                .setId(idGenerator.nextId())
                .setBizKey(orderNo)
                .setEventType(EVT_TICKET_ORDER_CREATE)
                .setExchangeName(TicketOrderContract.Exchange.TICKET_ORDER)
                .setRoutingKey(TicketOrderContract.RoutingKey.TICKET_ORDER_CREATE)
                .setPayload(payload)
                .setStatus(OutboxStatus.NEW)
                .setRetryCnt(MqConstants.DEFAULT_RETRY_COUNT)
                .setNextRetryTime(LocalDateTime.now())
                .setDeliverAt(deliverAt)
                .setBizExpireAt(bizExpireAt)
                .setTraceId(Span.current().getSpanContext().getTraceId());
    }
}

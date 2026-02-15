package com.meteor.ticketing.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import com.meteor.mq.contract.ticketing.TicketOrderDbReservedMessage;
import com.meteor.mq.core.MqSender;
import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.service.IMqOutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 *  MQ 输出箱调度任务
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-15 13:46
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqOutboxDispatcherJob {

    private final IMqOutboxEventService outboxService;
    private final MqSender mqSender;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY = 10;
    private static final Duration CONFIRM_TIMEOUT_MS = Duration.ofMillis(3000);

    @Scheduled(fixedDelay = 20000)
    public void dispatch() {
        List<MqOutboxEvent> due = outboxService.listDueEvents(BATCH_SIZE);
        if (due.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        for (MqOutboxEvent e : due) {
            try {
                if (now.isAfter(e.getBizExpireAt())) {
                    outboxService.markExpired(e.getId());
                    continue;
                }

                Object payloadObj = convertPayload(e);

                mqSender.sendAndWaitConfirm(
                        e.getExchangeName(),
                        e.getRoutingKey(),
                        payloadObj,
                        CONFIRM_TIMEOUT_MS
                );

                outboxService.markSent(e.getId());

            } catch (Exception ex) {
                handleFail(e, ex);
            }
        }
    }

    /**
     *  尝试转换 payload
     * */
    private Object convertPayload(MqOutboxEvent e) throws JsonProcessingException {
        return switch (e.getEventType()) {
            case "TICKET_ORDER_CREATE" ->
                    objectMapper.readValue(e.getPayload(), TicketOrderCreateMessage.class);
            case "TICKET_DB_RESERVED" ->
                    objectMapper.readValue(e.getPayload(), TicketOrderDbReservedMessage.class);
            default -> throw new IllegalArgumentException("Unknown eventType: " + e.getEventType());
        };
    }


    /**
     *  处理失败
     * */
    private void handleFail(MqOutboxEvent e, Exception ex) {
        LocalDateTime now = LocalDateTime.now();
        int nextRetry = (e.getRetryCnt() == null ? 0 : e.getRetryCnt()) + 1;
        String err = shorten(ex.getClass().getSimpleName() + ": " + ex.getMessage());

        if (nextRetry > MAX_RETRY) {
            outboxService.markDead(e.getId(), err);
            return;
        }

        LocalDateTime nextTime = now.plusSeconds(calcBackoffSeconds(nextRetry));
        outboxService.markFail(e.getId(), nextRetry, nextTime, err);
    }

    /**
     *  计算退避时间
     * */
    private long calcBackoffSeconds(int retryCnt) {
        long s = 1L << Math.min(retryCnt, 6);
        return Math.min(s, 60L);
    }

    /**
     *  截断字符串
     * */
    private String shorten(String s) {
        if (s == null) return null;
        return s.length() <= 500 ? s : s.substring(0, 500);
    }
}

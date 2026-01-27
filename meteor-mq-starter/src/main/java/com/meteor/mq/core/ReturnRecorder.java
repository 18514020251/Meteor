package com.meteor.mq.core;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录 RabbitMQ 回调结果
 *
 * @author Programmer
 * @date 2026-01-27 15:25
 */
public class ReturnRecorder {
    private final Map<String, ReturnedMessage> returned = new ConcurrentHashMap<>();

    public void record(String correlationId, ReturnedMessage message) {
        if (correlationId != null) {
            returned.put(correlationId, message);
        }
    }

    public Optional<ReturnedMessage> consume(String correlationId) {
        if (correlationId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(returned.remove(correlationId));
    }


    public static String extractCorrelationId(ReturnedMessage returned) {
        if (returned == null || returned.getMessage() == null) {
            return null;
        }

        MessageProperties props = returned.getMessage().getMessageProperties();

        Object headerCid = props.getHeaders().get("x-correlation-id");
        if (headerCid instanceof String s && !s.isBlank()) {
            return s;
        }

        String cid = props.getCorrelationId();
        return (cid == null || cid.isBlank()) ? null : cid;
    }
}

package com.meteor.mq.core;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *  MQ 发送者
 *
 * @author Programmer
 * @date 2026-01-21 15:08
 */
@RequiredArgsConstructor
public class MqSender {

    private final RabbitTemplate rabbitTemplate;
    private final ReturnRecorder returnRecorder;

    public void send(String exchange, String routingKey, Object payload) {
        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(exchange, routingKey, payload, cd);
    }

    public MqSendResult sendAndWaitConfirm(
            String exchange,
            String routingKey,
            Object payload,
            Duration timeout) {

        String cid = UUID.randomUUID().toString();
        CorrelationData cd = new CorrelationData(cid);

        rabbitTemplate.convertAndSend(exchange, routingKey, payload, message -> {
            message.getMessageProperties().setHeader("x-correlation-id", cid);

            message.getMessageProperties().setCorrelationId(cid);

            return message;
        }, cd);

        boolean ack;
        String cause;

        try {
            CorrelationData.Confirm confirm =
                    cd.getFuture().get(timeout.toMillis(), TimeUnit.MILLISECONDS);

            ack = confirm.isAck();
            cause = confirm.getReason();

        } catch (TimeoutException e) {
            ack = false;
            cause = "confirm_wait_timeout";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ack = false;
            cause = "confirm_wait_interrupted";

        } catch (ExecutionException e) {
            ack = false;
            cause = "confirm_wait_execution_failed: " + e.getCause();
        }

        ReturnedMessage returned =
                returnRecorder.consume(cid).orElse(null);

        return new MqSendResult(ack, cause, returned);
    }
}

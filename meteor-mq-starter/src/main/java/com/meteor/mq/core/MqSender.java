package com.meteor.mq.core;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 *  MQ 发送者
 *
 * @author Programmer
 * @date 2026-01-21 15:08
 */
@RequiredArgsConstructor
public class MqSender {

    private final RabbitTemplate rabbitTemplate;

    public void send(String exchange, String routingKey, Object payload) {
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }
}

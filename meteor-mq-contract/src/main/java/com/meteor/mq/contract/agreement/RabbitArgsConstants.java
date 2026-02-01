package com.meteor.mq.contract.agreement;

/**
 * RabbitMQ Queue arguments 常量
 *
 * @author Programmer
 */
public final class RabbitArgsConstants {

    private RabbitArgsConstants() {}

    public static final String X_MESSAGE_TTL = "x-message-ttl";
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
}

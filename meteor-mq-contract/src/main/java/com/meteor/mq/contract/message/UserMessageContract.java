package com.meteor.mq.contract.message;

import java.time.Duration;

/**
 * 用户消息 MQ 契约定义
 *
 * @author Programmer
 */
public final class UserMessageContract {

    private UserMessageContract() {}

    /** 消息发送 confirm 超时 */
    public static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(3);

    /** created 消息 TTL：7天 */
    public static final Duration CREATED_MSG_TTL = Duration.ofDays(7);

    public static final class Exchange {
        private Exchange() {}
        public static final String USER_MESSAGE = "user.message.exchange";
        public static final String USER_MESSAGE_DLX = "user.message.exchange.dlx";
    }

    public static final class Queue {
        private Queue() {}
        public static final String USER_MESSAGE_CREATED = "user.message.created.queue";
        public static final String USER_MESSAGE_CREATED_DLX = "user.message.created.dlx.queue";
    }

    public static final class RoutingKey {
        private RoutingKey() {}
        public static final String USER_MESSAGE_CREATED = "user.message.created";
        public static final String USER_MESSAGE_CREATED_DLX = "user.message.created.dlx";
    }
}

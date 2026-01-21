package com.meteor.mq.constant;

/**
 * MQ 相关常量定义
 * 只描述「消息协议」，不包含业务逻辑
 *
 * @author Programmer
 */
public final class MqConstants {

    private MqConstants() {}

    public static final class Exchange {
        public static final String MERCHANT_APPLY = "merchant.apply.exchange";
    }

    public static final class RoutingKey {
        public static final String MERCHANT_APPLY_CREATED = "merchant.apply.created";
    }

    public static final class Queue {
        public static final String MERCHANT_APPLY = "merchant.apply.queue";
    }
}


package com.meteor.mq.contract.merchant;

import java.time.Duration;

/**
 * 商家 MQ 契约定义
 *
 * @author Programmer
 */
public final class MerchantContract {

    private MerchantContract() {}

    /** 消息发送 confirm 超时 */
    public static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(3);

    /**
     * 商家激活事件
     */
    public static final Duration ACTIVATED_MSG_TTL = Duration.ofDays(50);

    public static final class Exchange {
        private Exchange() {}
        public static final String MERCHANT = "merchant.event.exchange";
        public static final String MERCHANT_DLX = "merchant.event.exchange.dlx";
    }

    public static final class Queue {
        private Queue() {}
        public static final String MERCHANT_ACTIVATED = "merchant.activated.queue";
        public static final String MERCHANT_ACTIVATED_DLX = "merchant.activated.dlx.queue";
    }

    public static final class RoutingKey {
        private RoutingKey() {}
        public static final String MERCHANT_ACTIVATED = "merchant.activated";
        public static final String MERCHANT_ACTIVATED_DLX = "merchant.activated.dlx";
    }
}

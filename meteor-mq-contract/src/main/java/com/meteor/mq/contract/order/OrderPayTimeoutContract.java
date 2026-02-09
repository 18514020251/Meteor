package com.meteor.mq.contract.order;

import java.time.Duration;

/**
 * 订单超时关单 MQ 契约（TTL + DLX）
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 19:07
 */
public final class OrderPayTimeoutContract {

    private OrderPayTimeoutContract() {}

    public static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(3);

    public static final Duration PAY_TTL = Duration.ofSeconds(120);

    public static final class Exchange {
        private Exchange() {}
        public static final String ORDER = "order.exchange";
        public static final String ORDER_DLX = "order.exchange.dlx";
    }

    public static final class Queue {
        private Queue() {}
        public static final String ORDER_PAY_TIMEOUT_DELAY = "order.pay.timeout.delay.queue";
        public static final String ORDER_PAY_TIMEOUT = "order.pay.timeout.queue";
    }

    public static final class RoutingKey {
        private RoutingKey() {}
        public static final String ORDER_PAY_TIMEOUT_DELAY = "order.pay.timeout.delay";
        public static final String ORDER_PAY_TIMEOUT = "order.pay.timeout";
    }
}

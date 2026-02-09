package com.meteor.mq.contract.ticketing;

import java.time.Duration;

/**
 * 抢票订单 MQ 契约
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 11:55
 */
public final class TicketOrderContract {

    private TicketOrderContract() {}

    public static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(3);

    public static final Duration ORDER_MSG_TTL = Duration.ofDays(1);

    public static final class Exchange {
        private Exchange() {}
        public static final String TICKET_ORDER = "ticket.order.exchange";
    }

    public static final class Queue {
        private Queue() {}
        public static final String TICKET_ORDER_CREATE = "ticket.order.create.queue";
    }

    public static final class RoutingKey {
        private RoutingKey() {}
        public static final String TICKET_ORDER_CREATE = "ticket.order.create";
    }
}

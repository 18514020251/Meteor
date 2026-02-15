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
        public static final String TICKET_ORDER_DLX = "ticket.order.exchange.dlx";
    }

    public static final class Queue {
        private Queue() {}
        public static final String TICKET_ORDER_CREATE = "ticket.order.create.queue";
        public static final String TICKET_ORDER_DB_RESERVED = "ticket.order.db.reserved";
        public static final String TICKET_ORDER_CREATE_DLX = "ticket.order.create.dlx.queue";
        public static final String TICKET_STOCK_RELEASE = "ticket.stock.release.queue";
    }

    public static final class RoutingKey {
        private RoutingKey() {}
        public static final String TICKET_ORDER_CREATE = "ticket.order.create";
        public static final String TICKET_ORDER_DB_RESERVED = "ticket.order.db.reserved.queue";
        public static final String TICKET_ORDER_CREATE_DLX = "ticket.order.create.dlx";
        public static final String TICKET_STOCK_RELEASE = "ticket.stock.release";
        public static final String TICKET_DB_RESERVED = "ticket.order.db_reserved";
    }
}


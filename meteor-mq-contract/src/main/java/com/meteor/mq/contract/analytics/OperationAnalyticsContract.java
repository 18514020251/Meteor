package com.meteor.mq.contract.analytics;

import java.time.Duration;

/**
 * 运营分析 MQ 契约
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11
 */
public final class OperationAnalyticsContract {

    private OperationAnalyticsContract() {}

    public static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(3);

    public static final class Exchange {
        private Exchange() {}
        public static final String ANALYTICS = "analytics.exchange";
    }

    public static final class Queue {
        private Queue() {}

        public static final String USER_REGISTERED = "analytics.user.registered.queue";

        public static final String PAY_CREATED = "analytics.pay.created.queue";

        public static final String PAY_SUCCESS = "analytics.pay.success.queue";
    }

    public static final class RoutingKey {
        private RoutingKey() {}

        public static final String USER_REGISTERED = "user.registered";

        public static final String PAY_CREATED = "pay.created";

        public static final String PAY_SUCCESS = "pay.success";
    }
}


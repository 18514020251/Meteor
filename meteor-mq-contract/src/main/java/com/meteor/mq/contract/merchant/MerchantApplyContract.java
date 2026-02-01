package com.meteor.mq.contract.merchant;

import java.time.Duration;

/**
 * 商家申请 MQ 契约定义
 *
 * @author Programmer
 */
public final class MerchantApplyContract {

    private MerchantApplyContract() {}

    public static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(3);

    public static final Duration CREATED_MSG_TTL = Duration.ofHours(24);

    public static final Duration USER_DEACTIVATED_MSG_TTL = Duration.ofHours(24);

    public static final class Exchange {
        private Exchange() {}
        public static final String MERCHANT_APPLY = "merchant.apply.exchange";
        public static final String MERCHANT_APPLY_DLX = "merchant.apply.exchange.dlx";

        public static final String USER_EVENT = "user.event.exchange";
        public static final String USER_EVENT_DLX = "user.event.exchange.dlx";
    }

    public static final class Queue {
        private Queue() {}
        public static final String MERCHANT_APPLY_CREATED = "merchant.apply.created.queue";
        public static final String MERCHANT_APPLY_REVIEWED = "merchant.apply.reviewed.queue";
        public static final String MERCHANT_APPLY_CREATED_DLX = "merchant.apply.created.dlx.queue";
        public static final String MERCHANT_APPLY_REVIEWED_DLX = "merchant.apply.reviewed.dlx.queue";

        public static final String USER_DEACTIVATED = "merchant.user.deactivated.queue";
        public static final String USER_DEACTIVATED_DLX = "merchant.user.deactivated.dlx.queue";
    }

    public static final class RoutingKey {
        private RoutingKey() {}
        public static final String MERCHANT_APPLY_CREATED = "merchant.apply.created";
        public static final String MERCHANT_APPLY_REVIEWED = "merchant.apply.reviewed";
        public static final String MERCHANT_APPLY_CREATED_DLX = "merchant.apply.created.dlx";
        public static final String MERCHANT_APPLY_REVIEWED_DLX = "merchant.apply.reviewed.dlx";

        public static final String USER_DEACTIVATED = "user.deactivated";
        public static final String USER_DEACTIVATED_DLX = "user.deactivated.dlx";
    }
}

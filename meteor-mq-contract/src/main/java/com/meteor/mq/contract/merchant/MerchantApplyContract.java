package com.meteor.mq.contract.merchant;

import java.time.Duration;

/**
 * 商家申请 MQ 契约定义
 * @author Programmer
 */
public final class MerchantApplyContract {

    private MerchantApplyContract() {}

    /** 消息发送 confirm 超时 */
    public static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(3);

    /** created 消息 TTL：24小时 */
    public static final Duration CREATED_MSG_TTL = Duration.ofHours(24);

    public static final class Exchange {
        private Exchange() {}
        public static final String MERCHANT_APPLY = "merchant.apply.exchange";
        public static final String MERCHANT_APPLY_DLX = "merchant.apply.exchange.dlx";
    }

    public static final class Queue {
        private Queue() {}
        public static final String MERCHANT_APPLY_CREATED = "merchant.apply.created.queue";
        public static final String MERCHANT_APPLY_REVIEWED = "merchant.apply.reviewed.queue";
        public static final String MERCHANT_APPLY_CREATED_DLX = "merchant.apply.created.dlx.queue";
        public static final String MERCHANT_APPLY_REVIEWED_DLX = "merchant.apply.reviewed.dlx.queue";
    }

    public static final class RoutingKey {
        private RoutingKey() {}
        public static final String MERCHANT_APPLY_CREATED = "merchant.apply.created";
        public static final String MERCHANT_APPLY_REVIEWED = "merchant.apply.reviewed";
        public static final String MERCHANT_APPLY_CREATED_DLX = "merchant.apply.created.dlx";
        public static final String MERCHANT_APPLY_REVIEWED_DLX = "merchant.apply.reviewed.dlx";
    }
}

package com.meteor.common.mq.merchant;

/**
 * 商家申请相关 MQ 协议
 * @author Programmer
 */
@SuppressWarnings("squid:S1118")
public final class MerchantApplyEvent {

    private MerchantApplyEvent() {}

    public static final class Exchange {
        public static final String MERCHANT_APPLY = "merchant.apply.exchange";
        public static final String MERCHANT_APPLY_DLX = "merchant.apply.exchange.dlx";
    }

    public static final class Queue {
        public static final String MERCHANT_APPLY_CREATED = "merchant.apply.created.queue";
        public static final String MERCHANT_APPLY_REVIEWED = "merchant.apply.reviewed.queue";
        public static final String MERCHANT_APPLY_CREATED_DLX = "merchant.apply.created.dlx.queue";
    }

    public static final class RoutingKey {
        public static final String MERCHANT_APPLY_CREATED = "merchant.apply.created";
        public static final String MERCHANT_APPLY_REVIEWED = "merchant.apply.reviewed";
        public static final String MERCHANT_APPLY_CREATED_DLX = "merchant.apply.created.dlx";
    }
}

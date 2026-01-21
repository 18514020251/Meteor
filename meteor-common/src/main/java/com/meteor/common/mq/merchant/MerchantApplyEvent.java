package com.meteor.common.mq.merchant;

/**
 * 商家申请相关 MQ 协议
 * @author Programmer
 */
public final class MerchantApplyEvent {

    private MerchantApplyEvent() {}

    public static final class Exchange {
        public static final String MERCHANT_APPLY = "merchant.apply.exchange";
    }

    public static final class RoutingKey {
        public static final String MERCHANT_APPLY_CREATED = "merchant.apply.created";
    }
}

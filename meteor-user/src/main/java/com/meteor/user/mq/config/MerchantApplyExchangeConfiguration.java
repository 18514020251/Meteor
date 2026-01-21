package com.meteor.user.mq.config;

import com.meteor.common.mq.merchant.MerchantApplyEvent;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  商家申请消息交换机配置类
 *
 * @author Programmer
 * @date 2026-01-21 17:03
 */
@Configuration
public class MerchantApplyExchangeConfiguration {

    @Bean
    public DirectExchange merchantApplyExchange() {
        return new DirectExchange(
                MerchantApplyEvent.Exchange.MERCHANT_APPLY,
                true,
                false
        );
    }
}

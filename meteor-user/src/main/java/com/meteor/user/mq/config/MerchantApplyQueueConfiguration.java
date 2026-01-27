package com.meteor.user.mq.config;

import com.meteor.common.mq.merchant.MerchantApplyEvent;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  商家申请队列配置
 *
 * @author Programmer
 * @date 2026-01-27 12:42
 */
@Configuration
public class MerchantApplyQueueConfiguration {

    @Bean
    public DirectExchange merchantApplyExchange() {
        return new DirectExchange(
                MerchantApplyEvent.Exchange.MERCHANT_APPLY,
                true,
                false
        );
    }


}

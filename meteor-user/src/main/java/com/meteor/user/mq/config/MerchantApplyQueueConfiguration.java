package com.meteor.user.mq.config;

import com.meteor.common.mq.merchant.MerchantApplyEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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

    @Bean
    public Queue merchantApplyCreatedQueue() {
        return new Queue(
                MerchantApplyEvent.Queue.MERCHANT_APPLY_CREATED,
                true,
                false,
                false
        );
    }

    @Bean
    public Binding merchantApplyCreatedBinding(Queue merchantApplyCreatedQueue,
                                               DirectExchange merchantApplyExchange) {
        return BindingBuilder.bind(merchantApplyCreatedQueue)
                .to(merchantApplyExchange)
                .with(MerchantApplyEvent.RoutingKey.MERCHANT_APPLY_CREATED);
    }


}

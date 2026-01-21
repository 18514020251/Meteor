package com.meteor.mq.autoconfigure;

import com.meteor.mq.constant.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 *  MQ 配置类
 *
 * @author Programmer
 * @date 2026-01-21 15:41
 */
@AutoConfiguration
public class MerchantApplyMqAutoConfiguration {

    @Bean
    public DirectExchange merchantApplyExchange() {
        return new DirectExchange(
                MqConstants.Exchange.MERCHANT_APPLY,
                true,
                false
        );
    }

    @Bean
    public Queue merchantApplyQueue() {
        return QueueBuilder
                .durable(MqConstants.Queue.MERCHANT_APPLY)
                .build();
    }

    @Bean
    public Binding merchantApplyBinding(
            DirectExchange merchantApplyExchange,
            Queue merchantApplyQueue
    ) {
        return BindingBuilder
                .bind(merchantApplyQueue)
                .to(merchantApplyExchange)
                .with(MqConstants.RoutingKey.MERCHANT_APPLY_CREATED);
    }
}



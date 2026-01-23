package com.meteor.admin.mq.comfig;

import com.meteor.common.mq.merchant.MerchantApplyEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  商家申请消息队列配置类
 *
 * @author Programmer
 * @date 2026-01-23 11:27
 */
@Configuration
public class MerchantApplyQueueConfiguration {

    @Bean
    public Queue merchantApplyCreatedQueue() {
        return new Queue(MerchantApplyEvent.Queue.MERCHANT_APPLY_CREATED, true);
    }

    @Bean
    public DirectExchange merchantApplyExchange() {
        return new DirectExchange(
                MerchantApplyEvent.Exchange.MERCHANT_APPLY,
                true,
                false
        );
    }

    @Bean
    public Binding merchantApplyCreatedBinding(Queue merchantApplyCreatedQueue, DirectExchange merchantApplyExchange) {
        return new Binding(
                merchantApplyCreatedQueue.getName(),
                Binding.DestinationType.QUEUE,
                merchantApplyExchange.getName(),
                MerchantApplyEvent.RoutingKey.MERCHANT_APPLY_CREATED,
                null
        );
    }
}


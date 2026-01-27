package com.meteor.admin.mq.config;

import com.meteor.common.mq.merchant.MerchantApplyEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 商家申请消息队列配置类
 *
 * @author Programmer
 * @date 2026-01-23 11:27
 */
@Configuration
public class MerchantApplyQueueConfiguration {

    private static final long CREATED_MSG_TTL_MS = 24L * 60 * 60 * 1000;

    @Bean
    public Queue merchantApplyCreatedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", CREATED_MSG_TTL_MS);

        args.put("x-dead-letter-exchange", MerchantApplyEvent.Exchange.MERCHANT_APPLY_DLX);
        args.put("x-dead-letter-routing-key", MerchantApplyEvent.RoutingKey.MERCHANT_APPLY_CREATED_DLX);

        return new Queue(MerchantApplyEvent.Queue.MERCHANT_APPLY_CREATED, true, false, false, args);
    }

    @Bean
    public DirectExchange merchantApplyDlxExchange() {
        return new DirectExchange(
                MerchantApplyEvent.Exchange.MERCHANT_APPLY_DLX,
                true,
                false
        );
    }

    /**
     * DLQ：死信队列
     */
    @Bean
    public Queue merchantApplyCreatedDlq() {
        return new Queue(MerchantApplyEvent.Queue.MERCHANT_APPLY_CREATED_DLX, true);
    }

    /**
     * DLQ 绑定
     */
    @Bean
    public Binding merchantApplyCreatedDlqBinding(Queue merchantApplyCreatedDlq,
                                                  DirectExchange merchantApplyDlxExchange) {
        return BindingBuilder.bind(merchantApplyCreatedDlq)
                .to(merchantApplyDlxExchange)
                .with(MerchantApplyEvent.RoutingKey.MERCHANT_APPLY_CREATED_DLX);
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
    public Binding merchantApplyCreatedBinding(Queue merchantApplyCreatedQueue,
                                               DirectExchange merchantApplyExchange) {
        return BindingBuilder.bind(merchantApplyCreatedQueue)
                .to(merchantApplyExchange)
                .with(MerchantApplyEvent.RoutingKey.MERCHANT_APPLY_CREATED);
    }

}


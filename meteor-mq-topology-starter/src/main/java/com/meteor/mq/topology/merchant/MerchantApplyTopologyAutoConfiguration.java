package com.meteor.mq.topology.merchant;

import com.meteor.mq.contract.merchant.MerchantApplyContract;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 *  商家申请拓扑声明
 *
 * @author Programmer
 * @date 2026-01-28 11:17
 */
@AutoConfiguration
@EnableConfigurationProperties(MerchantApplyTopologyProperties.class)
@ConditionalOnProperty(prefix = "meteor.mq.topology.merchant-apply", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MerchantApplyTopologyAutoConfiguration {

    @Bean
    public DirectExchange merchantApplyExchange() {
        return new DirectExchange(MerchantApplyContract.Exchange.MERCHANT_APPLY, true, false);
    }

    @Bean
    public DirectExchange merchantApplyDlxExchange() {
        return new DirectExchange(MerchantApplyContract.Exchange.MERCHANT_APPLY_DLX, true, false);
    }

    @Bean
    public Queue merchantApplyCreatedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", MerchantApplyContract.CREATED_MSG_TTL.toMillis());
        args.put("x-dead-letter-exchange", MerchantApplyContract.Exchange.MERCHANT_APPLY_DLX);
        args.put("x-dead-letter-routing-key", MerchantApplyContract.RoutingKey.MERCHANT_APPLY_CREATED_DLX);

        return new Queue(MerchantApplyContract.Queue.MERCHANT_APPLY_CREATED, true, false, false, args);
    }

    @Bean
    public Binding merchantApplyCreatedBinding(Queue merchantApplyCreatedQueue,
                                               DirectExchange merchantApplyExchange) {
        return BindingBuilder.bind(merchantApplyCreatedQueue)
                .to(merchantApplyExchange)
                .with(MerchantApplyContract.RoutingKey.MERCHANT_APPLY_CREATED);
    }

    @Bean
    public Queue merchantApplyReviewedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", MerchantApplyContract.Exchange.MERCHANT_APPLY_DLX);
        args.put("x-dead-letter-routing-key", MerchantApplyContract.RoutingKey.MERCHANT_APPLY_REVIEWED_DLX);
        return new Queue(MerchantApplyContract.Queue.MERCHANT_APPLY_REVIEWED, true, false, false, args);
    }

    @Bean
    public Binding merchantApplyReviewedBinding(Queue merchantApplyReviewedQueue,
                                                DirectExchange merchantApplyExchange) {
        return BindingBuilder.bind(merchantApplyReviewedQueue)
                .to(merchantApplyExchange)
                .with(MerchantApplyContract.RoutingKey.MERCHANT_APPLY_REVIEWED);
    }

    @Bean
    public Queue merchantApplyCreatedDlq() {
        return new Queue(MerchantApplyContract.Queue.MERCHANT_APPLY_CREATED_DLX, true);
    }

    @Bean
    public Binding merchantApplyCreatedDlqBinding(Queue merchantApplyCreatedDlq,
                                                  DirectExchange merchantApplyDlxExchange) {
        return BindingBuilder.bind(merchantApplyCreatedDlq)
                .to(merchantApplyDlxExchange)
                .with(MerchantApplyContract.RoutingKey.MERCHANT_APPLY_CREATED_DLX);
    }

    @Bean
    public Queue merchantApplyReviewedDlq() {
        return new Queue(MerchantApplyContract.Queue.MERCHANT_APPLY_REVIEWED_DLX, true);
    }

    @Bean
    public Binding merchantApplyReviewedDlqBinding(Queue merchantApplyReviewedDlq,
                                                   DirectExchange merchantApplyDlxExchange) {
        return BindingBuilder.bind(merchantApplyReviewedDlq)
                .to(merchantApplyDlxExchange)
                .with(MerchantApplyContract.RoutingKey.MERCHANT_APPLY_REVIEWED_DLX);
    }
}

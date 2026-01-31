package com.meteor.mq.topology.merchant;

import com.meteor.mq.contract.merchant.MerchantContract;
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
 * 商家 MQ 拓扑声明
 *
 * @author Programmer
 */
@AutoConfiguration
@EnableConfigurationProperties(MerchantTopologyProperties.class)
@ConditionalOnProperty(
        prefix = "meteor.mq.topology.merchant",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MerchantTopologyAutoConfiguration {

    @Bean
    public DirectExchange merchantExchange() {
        return new DirectExchange(
                MerchantContract.Exchange.MERCHANT,
                true,
                false
        );
    }

    @Bean
    public DirectExchange merchantDlxExchange() {
        return new DirectExchange(
                MerchantContract.Exchange.MERCHANT_DLX,
                true,
                false
        );
    }

    @Bean
    public Queue merchantActivatedQueue() {
        Map<String, Object> args = new HashMap<>();

        args.put("x-message-ttl", MerchantContract.ACTIVATED_MSG_TTL.toMillis());
        args.put("x-dead-letter-exchange", MerchantContract.Exchange.MERCHANT_DLX);
        args.put("x-dead-letter-routing-key", MerchantContract.RoutingKey.MERCHANT_ACTIVATED_DLX);

        return new Queue(
                MerchantContract.Queue.MERCHANT_ACTIVATED,
                true,
                false,
                false,
                args
        );
    }

    @Bean
    public Binding merchantActivatedBinding(
            Queue merchantActivatedQueue,
            DirectExchange merchantExchange
    ) {
        return BindingBuilder.bind(merchantActivatedQueue)
                .to(merchantExchange)
                .with(MerchantContract.RoutingKey.MERCHANT_ACTIVATED);
    }

    @Bean
    public Queue merchantActivatedDlq() {
        return new Queue(
                MerchantContract.Queue.MERCHANT_ACTIVATED_DLX,
                true
        );
    }

    @Bean
    public Binding merchantActivatedDlqBinding(
            Queue merchantActivatedDlq,
            DirectExchange merchantDlxExchange
    ) {
        return BindingBuilder.bind(merchantActivatedDlq)
                .to(merchantDlxExchange)
                .with(MerchantContract.RoutingKey.MERCHANT_ACTIVATED_DLX);
    }
}
package com.meteor.mq.topology.order;

import com.meteor.mq.contract.order.OrderPayTimeoutContract;
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
 * 订单超时关单拓扑（TTL + DLX）
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 19:08
 */
@AutoConfiguration
@EnableConfigurationProperties(OrderPayTimeoutTopologyProperties.class)
@ConditionalOnProperty(
        prefix = "meteor.mq.topology.order-timeout",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OrderPayTimeoutTopologyAutoConfiguration {

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(OrderPayTimeoutContract.Exchange.ORDER, true, false);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(OrderPayTimeoutContract.Exchange.ORDER_DLX, true, false);
    }

    /**
     * 延时队列：消息进来后待 PAY_TTL，到期变死信，转发到 ORDER_DLX + routingKey=ORDER_PAY_TIMEOUT
     */
    @Bean
    public Queue orderPayTimeoutDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", OrderPayTimeoutContract.PAY_TTL.toMillis());
        args.put("x-dead-letter-exchange", OrderPayTimeoutContract.Exchange.ORDER_DLX);
        args.put("x-dead-letter-routing-key", OrderPayTimeoutContract.RoutingKey.ORDER_PAY_TIMEOUT);
        return new Queue(OrderPayTimeoutContract.Queue.ORDER_PAY_TIMEOUT_DELAY, true, false, false, args);
    }

    @Bean
    public Binding orderPayTimeoutDelayBinding(Queue orderPayTimeoutDelayQueue,
                                               DirectExchange orderExchange) {
        return BindingBuilder.bind(orderPayTimeoutDelayQueue)
                .to(orderExchange)
                .with(OrderPayTimeoutContract.RoutingKey.ORDER_PAY_TIMEOUT_DELAY);
    }

    /**
     */
    @Bean
    public Queue orderPayTimeoutQueue() {
        return new Queue(OrderPayTimeoutContract.Queue.ORDER_PAY_TIMEOUT, true);
    }

    @Bean
    public Binding orderPayTimeoutBinding(Queue orderPayTimeoutQueue,
                                          DirectExchange orderDlxExchange) {
        return BindingBuilder.bind(orderPayTimeoutQueue)
                .to(orderDlxExchange)
                .with(OrderPayTimeoutContract.RoutingKey.ORDER_PAY_TIMEOUT);
    }
}

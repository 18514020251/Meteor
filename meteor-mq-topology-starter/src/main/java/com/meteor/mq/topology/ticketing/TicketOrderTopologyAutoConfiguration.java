package com.meteor.mq.topology.ticketing;

import com.meteor.mq.contract.ticketing.TicketOrderContract;
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
 * 抢票订单拓扑声明
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 12:02
 */
@AutoConfiguration
@EnableConfigurationProperties(TicketOrderTopologyProperties.class)
@ConditionalOnProperty(
        prefix = "meteor.mq.topology.ticket-order",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class TicketOrderTopologyAutoConfiguration {

    // 主交换机
    @Bean
    public DirectExchange ticketOrderExchange() {
        return new DirectExchange(TicketOrderContract.Exchange.TICKET_ORDER, true, false);
    }

    // 死信交换机
    @Bean
    public DirectExchange ticketOrderDlxExchange() {
        return new DirectExchange(TicketOrderContract.Exchange.TICKET_ORDER_DLX, true, false);
    }

    // 主队列（带DLX配置）
    @Bean
    public Queue ticketOrderCreateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", TicketOrderContract.ORDER_MSG_TTL.toMillis());
        args.put("x-dead-letter-exchange", TicketOrderContract.Exchange.TICKET_ORDER_DLX);
        args.put("x-dead-letter-routing-key", TicketOrderContract.RoutingKey.TICKET_ORDER_CREATE_DLX);

        return new Queue(TicketOrderContract.Queue.TICKET_ORDER_CREATE, true, false, false, args);
    }

    // 主绑定
    @Bean
    public Binding ticketOrderCreateBinding(
            Queue ticketOrderCreateQueue,
            DirectExchange ticketOrderExchange
    ) {
        return BindingBuilder.bind(ticketOrderCreateQueue)
                .to(ticketOrderExchange)
                .with(TicketOrderContract.RoutingKey.TICKET_ORDER_CREATE);
    }

    // 死信队列
    @Bean
    public Queue ticketOrderCreateDlq() {
        return new Queue(TicketOrderContract.Queue.TICKET_ORDER_CREATE_DLX, true);
    }

    // 死信绑定
    @Bean
    public Binding ticketOrderCreateDlqBinding(
            Queue ticketOrderCreateDlq,
            DirectExchange ticketOrderDlxExchange
    ) {
        return BindingBuilder.bind(ticketOrderCreateDlq)
                .to(ticketOrderDlxExchange)
                .with(TicketOrderContract.RoutingKey.TICKET_ORDER_CREATE_DLX);
    }

    @Bean
    public Queue ticketOrderDbReservedQueue() {
        return new Queue(
                TicketOrderContract.Queue.TICKET_ORDER_DB_RESERVED,
                true
        );
    }

    @Bean
    public Binding ticketOrderDbReservedBinding(
            Queue ticketOrderDbReservedQueue,
            DirectExchange ticketOrderExchange
    ) {
        return BindingBuilder.bind(ticketOrderDbReservedQueue)
                .to(ticketOrderExchange)
                .with(TicketOrderContract.RoutingKey.TICKET_ORDER_DB_RESERVED);
    }

    @Bean
    public Queue ticketStockReleaseQueue() {
        return new Queue(TicketOrderContract.Queue.TICKET_STOCK_RELEASE, true);
    }

    @Bean
    public Binding ticketStockReleaseBinding(
            Queue ticketStockReleaseQueue,
            DirectExchange ticketOrderExchange
    ) {
        return BindingBuilder.bind(ticketStockReleaseQueue)
                .to(ticketOrderExchange)
                .with(TicketOrderContract.RoutingKey.TICKET_STOCK_RELEASE);
    }

}

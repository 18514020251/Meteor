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

    @Bean
    public DirectExchange ticketOrderExchange() {
        return new DirectExchange(TicketOrderContract.Exchange.TICKET_ORDER, true, false);
    }

    @Bean
    public Queue ticketOrderCreateQueue() {
        return new Queue(TicketOrderContract.Queue.TICKET_ORDER_CREATE, true);
    }

    @Bean
    public Binding ticketOrderCreateBinding(
            Queue ticketOrderCreateQueue,
            DirectExchange ticketOrderExchange
    ) {
        return BindingBuilder.bind(ticketOrderCreateQueue)
                .to(ticketOrderExchange)
                .with(TicketOrderContract.RoutingKey.TICKET_ORDER_CREATE);
    }
}
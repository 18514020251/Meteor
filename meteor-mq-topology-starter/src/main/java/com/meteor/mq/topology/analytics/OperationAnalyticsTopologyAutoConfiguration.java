package com.meteor.mq.topology.analytics;

import com.meteor.mq.contract.analytics.OperationAnalyticsContract;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 运营分析拓扑
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11
 */
@AutoConfiguration
@EnableConfigurationProperties(OperationAnalyticsTopologyProperties.class)
@ConditionalOnProperty(
        prefix = "meteor.mq.topology.analytics",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OperationAnalyticsTopologyAutoConfiguration {

    @Bean
    public TopicExchange analyticsExchange() {
        return new TopicExchange(OperationAnalyticsContract.Exchange.ANALYTICS, true, false);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(OperationAnalyticsContract.Queue.USER_REGISTERED, true);
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue,
                                         TopicExchange analyticsExchange) {
        return BindingBuilder.bind(userRegisteredQueue)
                .to(analyticsExchange)
                .with(OperationAnalyticsContract.RoutingKey.USER_REGISTERED);
    }


    @Bean
    public Queue payCreatedQueue() {
        return new Queue(OperationAnalyticsContract.Queue.PAY_CREATED, true);
    }

    @Bean
    public Binding payCreatedBinding(Queue payCreatedQueue,
                                     TopicExchange analyticsExchange) {
        return BindingBuilder.bind(payCreatedQueue)
                .to(analyticsExchange)
                .with(OperationAnalyticsContract.RoutingKey.PAY_CREATED);
    }


    @Bean
    public Queue paySuccessQueue() {
        return new Queue(OperationAnalyticsContract.Queue.PAY_SUCCESS, true);
    }

    @Bean
    public Binding paySuccessBinding(Queue paySuccessQueue,
                                     TopicExchange analyticsExchange) {
        return BindingBuilder.bind(paySuccessQueue)
                .to(analyticsExchange)
                .with(OperationAnalyticsContract.RoutingKey.PAY_SUCCESS);
    }
}

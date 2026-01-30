package com.meteor.mq.topology.message;

import com.meteor.mq.contract.message.UserMessageContract;
import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户消息拓扑声明
 *
 * @author Programmer
 */
@AutoConfiguration
@EnableConfigurationProperties(UserMessageTopologyProperties.class)
@ConditionalOnProperty(prefix = "meteor.mq.topology.user-message", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UserMessageTopologyAutoConfiguration {

    @Bean
    public DirectExchange userMessageExchange() {
        return new DirectExchange(UserMessageContract.Exchange.USER_MESSAGE, true, false);
    }

    @Bean
    public DirectExchange userMessageDlxExchange() {
        return new DirectExchange(UserMessageContract.Exchange.USER_MESSAGE_DLX, true, false);
    }

    @Bean
    public Queue userMessageCreatedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", UserMessageContract.CREATED_MSG_TTL.toMillis());
        args.put("x-dead-letter-exchange", UserMessageContract.Exchange.USER_MESSAGE_DLX);
        args.put("x-dead-letter-routing-key", UserMessageContract.RoutingKey.USER_MESSAGE_CREATED_DLX);

        return new Queue(UserMessageContract.Queue.USER_MESSAGE_CREATED, true, false, false, args);
    }

    @Bean
    public Binding userMessageCreatedBinding(Queue userMessageCreatedQueue,
                                             DirectExchange userMessageExchange) {
        return BindingBuilder.bind(userMessageCreatedQueue)
                .to(userMessageExchange)
                .with(UserMessageContract.RoutingKey.USER_MESSAGE_CREATED);
    }

    @Bean
    public Queue userMessageCreatedDlq() {
        return new Queue(UserMessageContract.Queue.USER_MESSAGE_CREATED_DLX, true);
    }

    @Bean
    public Binding userMessageCreatedDlqBinding(Queue userMessageCreatedDlq,
                                                DirectExchange userMessageDlxExchange) {
        return BindingBuilder.bind(userMessageCreatedDlq)
                .to(userMessageDlxExchange)
                .with(UserMessageContract.RoutingKey.USER_MESSAGE_CREATED_DLX);
    }
}

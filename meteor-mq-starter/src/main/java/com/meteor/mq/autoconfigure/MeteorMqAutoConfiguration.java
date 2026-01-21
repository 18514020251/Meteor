package com.meteor.mq.autoconfigure;

import com.meteor.mq.core.MqSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * MQ 配置类
 *
 * @author Programmer
 */
@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
@Slf4j
public class MeteorMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public MqSender mqSender(RabbitTemplate rabbitTemplate) {
        return new MqSender(rabbitTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplateCustomizer rabbitTemplateCustomizer() {
        return rabbitTemplate -> {

            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                if (!ack) {
                    log.error("MQ 消息发送失败，cause={}", cause);
                }
            });

            rabbitTemplate.setReturnsCallback(returned -> log.error(
                    "MQ 消息被退回 exchange={}, routingKey={}, replyText={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyText()
            ));
        };
    }

}

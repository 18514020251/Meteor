package com.meteor.mq.autoconfigure;

import com.meteor.mq.core.MqSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
}

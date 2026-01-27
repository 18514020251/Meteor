package com.meteor.mq.autoconfigure;

import com.meteor.mq.core.MqSender;
import com.meteor.mq.core.ReturnRecorder;
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
    @ConditionalOnMissingBean
    public ReturnRecorder returnRecorder() {
        return new ReturnRecorder();
    }

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public MqSender mqSender(RabbitTemplate rabbitTemplate,
                             ReturnRecorder returnRecorder) {
        return new MqSender(rabbitTemplate, returnRecorder);
    }

    @Bean
    public RabbitTemplateCustomizer rabbitTemplateCustomizer(ReturnRecorder returnRecorder,
                                                             MessageConverter messageConverter) {
        return rabbitTemplate -> {

            rabbitTemplate.setMessageConverter(messageConverter);

            rabbitTemplate.setMandatory(true);

            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                String cid = correlationData == null ? null : correlationData.getId();
                if (!ack) {
                    log.error("MQ confirm failed cid={}, cause={}", cid, cause);
                } else {
                    log.debug("MQ confirm ok cid={}", cid);
                }
            });

            rabbitTemplate.setReturnsCallback(returned -> {
                String cid = ReturnRecorder.extractCorrelationId(returned);
                returnRecorder.record(cid, returned);
                log.error("MQ returned cid={}, exchange={}, routingKey={}, replyCode={}, replyText={}",
                        cid, returned.getExchange(), returned.getRoutingKey(),
                        returned.getReplyCode(), returned.getReplyText());
            });
        };
    }
}

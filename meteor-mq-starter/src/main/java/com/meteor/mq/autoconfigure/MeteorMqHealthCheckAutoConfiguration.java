package com.meteor.mq.autoconfigure;

import com.meteor.mq.startup.RabbitConnectionChecker;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 *  Mq 启动连接测试
 *
 * @author Programmer
 * @date 2026-01-28 19:54
 */
@AutoConfiguration
@ConditionalOnClass(ConnectionFactory.class)
@ConditionalOnProperty(prefix = "meteor.mq.health-check", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MeteorMqHealthCheckAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RabbitConnectionChecker.class)
    public RabbitConnectionChecker rabbitConnectionChecker(ConnectionFactory connectionFactory, Environment env) {
        return new RabbitConnectionChecker(connectionFactory, env);
    }
}

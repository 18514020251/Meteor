package com.meteor.redis.autoconfigure;

import com.meteor.redis.autoconfigure.properties.MeteorRedisProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * 自定义Redis连接工厂
 * Redis 配置文件
 * 确保正确传递密码
 *
 * @author Programmer
 */
@AutoConfiguration
@AutoConfigureBefore(RedisAutoConfiguration.class)
@EnableConfigurationProperties(MeteorRedisProperties.class)
@ConditionalOnClass(LettuceConnectionFactory.class)
public class MeteorRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public LettuceConnectionFactory redisConnectionFactory(
            MeteorRedisProperties props) {

        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(
                        props.getHost(),
                        props.getPort()
                );

        if (props.getPassword() != null && !props.getPassword().isBlank()) {
            config.setPassword(RedisPassword.of(props.getPassword()));
        }

        LettuceClientConfiguration clientConfig =
                LettuceClientConfiguration.builder()
                        .commandTimeout(props.getTimeout())
                        .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }
}
package com.meteor.redis.autoconfigure;

import com.meteor.redis.startup.RedisConnectionChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Programmer
 * @date 2026-01-28 19:30
 */
@AutoConfiguration
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "meteor.redis.health-check", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MeteorRedisHealthCheckAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisConnectionChecker.class)
    public RedisConnectionChecker redisConnectionChecker(StringRedisTemplate template, Environment env) {
        return new RedisConnectionChecker(template, env);
    }
}

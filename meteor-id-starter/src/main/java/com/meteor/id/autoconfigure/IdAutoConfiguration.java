package com.meteor.id.autoconfigure;

import com.meteor.id.properties.IdProperties;
import com.meteor.id.utils.SnowflakeIdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Programmer
 * @date 2026-01-30 16:27
 */
@Configuration
@EnableConfigurationProperties(IdProperties.class)
public class IdAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeIdGenerator snowflakeIdGenerator(IdProperties properties) {
        return new SnowflakeIdGenerator(properties.getWorkerId(), properties.getDatacenterId());
    }
}

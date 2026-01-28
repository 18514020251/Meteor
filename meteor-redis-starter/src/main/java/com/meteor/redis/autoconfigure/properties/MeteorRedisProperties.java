package com.meteor.redis.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

import java.time.Duration;

/**
 * 接收配置文件参数
 *
 * @author Programmer
 * @date 2026-01-17 12:46
 */
@Data
@ConfigurationProperties(prefix = "spring.data.redis")
public class MeteorRedisProperties {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private String host;
    private int port;
    private String password;
    private Duration timeout = DEFAULT_TIMEOUT;

}

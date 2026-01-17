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
@ConfigurationProperties(prefix = "spring.redis")
public class MeteorRedisProperties {

    private String host;
    private int port;
    private String password;
    private Duration timeout = Duration.ofSeconds(10);


}

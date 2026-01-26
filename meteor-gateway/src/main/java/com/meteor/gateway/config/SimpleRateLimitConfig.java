package com.meteor.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 *  限流配置
 *
 * @author Programmer
 * @date 2026-01-26 23:43
 */
@Configuration
public class SimpleRateLimitConfig {
// todo: 后续优化代--> 优先获取并存token
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = Objects.requireNonNull(exchange.getRequest()
                            .getRemoteAddress())
                    .getAddress()
                    .getHostAddress();
            return Mono.just(ip);
        };
    }
}


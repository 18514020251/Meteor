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

    @Bean
    public KeyResolver tokenOrIpKeyResolver() {
        return exchange -> {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (token != null && !token.isBlank()) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                return Mono.just("tk:" + token);
            }
            String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                    .getAddress().getHostAddress();
            return Mono.just("ip:" + ip);
        };
    }

}

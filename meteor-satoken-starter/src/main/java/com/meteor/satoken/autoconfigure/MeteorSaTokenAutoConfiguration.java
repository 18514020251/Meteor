package com.meteor.satoken.autoconfigure;

import cn.dev33.satoken.config.SaTokenConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * sa-token 配置类
 *
 * @author Programmer
 * @Date 2026-01-16 15:12:00
 */
@AutoConfiguration
public class MeteorSaTokenAutoConfiguration {

    private static final long TOKEN_TIMEOUT_ONE_DAY = 60L * 60 * 24;

    @Bean
    @ConditionalOnMissingBean
    public SaTokenConfig saTokenConfig() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenName("Authorization");
        config.setTimeout(TOKEN_TIMEOUT_ONE_DAY);
        config.setIsConcurrent(true);
        config.setIsShare(true);
        config.setTokenStyle("uuid");
        return config;
    }
}

package com.meteor.satoken.autoconfigure;

import cn.dev33.satoken.config.SaTokenConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MeteorSaTokenAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SaTokenConfig saTokenConfig() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenName("Authorization");
        config.setTimeout(60 * 60 * 24);
        config.setIsConcurrent(true);
        config.setIsShare(true);
        config.setTokenStyle("uuid");
        return config;
    }
}

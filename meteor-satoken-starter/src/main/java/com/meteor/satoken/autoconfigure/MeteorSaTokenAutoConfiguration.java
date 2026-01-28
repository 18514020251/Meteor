package com.meteor.satoken.autoconfigure;

import cn.dev33.satoken.config.SaTokenConfig;
import com.meteor.satoken.context.LoginContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * sa-token 配置类
 * @author Programmer
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

    @Bean
    @ConditionalOnMissingBean
    public LoginContext loginContext() {
        return new LoginContext();
    }
}

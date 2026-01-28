package com.meteor.satoken.autoconfigure;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *  Sa-Token 拦截器 配置类
 *
 * @author Programmer
 */
@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer.class)
public class MeteorSaTokenMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SaInterceptor.class)
    public SaInterceptor saInterceptor() {
        return new SaInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "meteorSaTokenMvcConfigurer")
    public WebMvcConfigurer meteorSaTokenMvcConfigurer(SaInterceptor saInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(saInterceptor).addPathPatterns("/**");
            }
        };
    }
}

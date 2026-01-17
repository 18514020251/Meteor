package com.meteor.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import com.meteor.common.result.Result;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa 拦截器
 *
 * @author Programmer
 * @date 2026-01-16 23:18
 */

@Configuration
public class SaTokenGatewayConfig {

    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                // 放行登录、注册
                .addExclude(
                        "/user/login",
                        "/user/register",
                        "/error",
                        "/swagger-ui.html"
                )
                // 鉴权逻辑
                .setAuth(obj -> StpUtil.checkLogin())
                .setError( e -> Result.fail(e.getMessage()));
    }
}

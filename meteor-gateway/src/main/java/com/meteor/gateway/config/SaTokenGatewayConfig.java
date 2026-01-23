package com.meteor.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.result.Result;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  Sa-Token 配置
 *
 * @author Programmer
 */
@Configuration
public class SaTokenGatewayConfig {

    private final ObjectMapper objectMapper;

    public SaTokenGatewayConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(
                        "/user/login",
                        "/user/register",
                        "/error"
                )
                .setAuth(obj -> {
                    StpUtil.checkLogin();

                    String path = SaHolder.getRequest().getRequestPath();
                    if (path.startsWith("/admin")) {
                        StpUtil.checkRole("admin");
                    }
                })
                .setError(this::handleSaTokenError);
    }

    private String handleSaTokenError(Throwable e) {

        Result<Void> result;

        if (e instanceof NotLoginException) {
            result = Result.fail(CommonErrorCode.UNAUTHORIZED);
        } else if (e instanceof NotPermissionException) {
            result = Result.fail(CommonErrorCode.FORBIDDEN);
        } else {
            result = Result.fail(CommonErrorCode.SYSTEM_ERROR);
        }

        SaHolder.getResponse()
                .setHeader("Content-Type", "application/json;charset=UTF-8");

        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            return "{\"code\":500,\"message\":\"系统异常\",\"data\":null}";
        }
    }
}

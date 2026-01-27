package com.meteor.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.result.Result;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import cn.dev33.satoken.exception.NotRoleException;

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
                        "/user/phone/code",
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
        int httpStatus;

        if (e instanceof NotLoginException) {
            result = Result.fail(CommonErrorCode.UNAUTHORIZED);
            httpStatus = 401;
        } else if (e instanceof NotRoleException) {
            result = Result.fail(CommonErrorCode.FORBIDDEN);
            httpStatus = 403;
        } else {
            result = Result.fail(CommonErrorCode.SYSTEM_ERROR);
            httpStatus = 500;
        }

        SaHolder.getResponse()
                .setStatus(httpStatus)
                .setHeader("Content-Type", "application/json;charset=UTF-8");

        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            return "{\"code\":500,\"msg\":\"系统异常\",\"data\":null}";
        }
    }
}

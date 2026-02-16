package com.meteor.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.result.Result;
import com.meteor.satoken.constants.RoleConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import cn.dev33.satoken.exception.NotRoleException;

/**
 *  Sa-Token 配置
 *
 * @author Programmer
 */
@Configuration
@Slf4j
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
                        "/merchant/getInfo",
                        "/movies/categories",
                        "/movies/latest",
                        "/movies/**",
                        "/ticketing/screenings/movie/**",
                        "/ticketing/screenings/**",

                        "/order/pay/confirm",
                        "/order/pay/confirm/**",

                        "/error"
                )
                .setBeforeAuth(obj -> {
                    SaHolder.getResponse()
                            .setHeader("Access-Control-Allow-Origin", SaHolder.getRequest().getHeader("Origin"))
                            .setHeader("Access-Control-Allow-Credentials", "true")
                            .setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                            .setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

                    if ("OPTIONS".equalsIgnoreCase(SaHolder.getRequest().getMethod())) {
                        SaHolder.getResponse().setStatus(200);
                    }
                })
                .setAuth(obj -> {
                    StpUtil.checkLogin();

                    String path = SaHolder.getRequest().getRequestPath();
                    if (path.startsWith("/admin")) {
                        StpUtil.checkRole(RoleConst.ADMIN);
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

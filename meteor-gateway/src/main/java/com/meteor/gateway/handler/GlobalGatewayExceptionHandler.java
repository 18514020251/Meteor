package com.meteor.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.common.result.Result;
import lombok.NonNull;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 全局网关异常处理器
 *
 * @author Programmer
 * @date 2026-01-17 18:51
 */
@Order(-1)
@Configuration
public class GlobalGatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable ex) {

        // 已提交就不处理
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        var response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setAcceptCharset(
                java.util.List.of(StandardCharsets.UTF_8)
        );

        Result<Void> result;

        if (ex instanceof cn.dev33.satoken.exception.NotLoginException) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            result = Result.fail(401, "未登录");
        }
        else if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            result = Result.fail(503, "服务不可用，请稍后重试");
        }
        else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            result = Result.fail(500, "系统异常");
        }

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (Exception e) {
            bytes = "{\"code\":500,\"msg\":\"系统异常\"}".getBytes(StandardCharsets.UTF_8);
        }

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes))
        );
    }
}

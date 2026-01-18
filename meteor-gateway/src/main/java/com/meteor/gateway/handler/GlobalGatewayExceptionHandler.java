package com.meteor.gateway.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.result.Result;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        var response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setAcceptCharset(List.of(StandardCharsets.UTF_8));

        CommonErrorCode errorCode = resolveErrorCode(ex);

        response.setStatusCode(errorCode.getHttpStatus());

        Result<Void> result = Result.fail(errorCode);

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


    private CommonErrorCode resolveErrorCode(Throwable ex) {

        if (ex instanceof NotLoginException) {
            return CommonErrorCode.UNAUTHORIZED;
        }

        if (ex instanceof NotFoundException) {
            return CommonErrorCode.SERVICE_UNAVAILABLE;
        }

        return CommonErrorCode.SYSTEM_ERROR;
    }

}

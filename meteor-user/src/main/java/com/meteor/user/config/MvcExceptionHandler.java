package com.meteor.user.config;

import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Spring MVC 专用异常处理器
 *
 * @author Programmer
 * @date 2026-01-19
 */
@Slf4j
@RestControllerAdvice
public class MvcExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("未找到该资源：{}", e.getMessage());
        return Result.fail(CommonErrorCode.NOT_FOUND);
    }
}

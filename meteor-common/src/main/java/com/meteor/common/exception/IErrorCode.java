package com.meteor.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 错误码接口
 *
 * @author Programmer
 * @date 2026-01-17 15:50
 */
public interface IErrorCode {

    int getCode();

    String getMessage();

    default HttpStatus getHttpStatus() {
        return HttpStatus.OK;
    }
}


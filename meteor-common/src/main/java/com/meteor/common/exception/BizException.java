package com.meteor.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author Programmer
 * @date 2026-01-17 15:32
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(IErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }


}

package com.meteor.common.exception;

/**
 * 基础异常类
 *
 * @author Programmer
 * @date 2026-01-16 16:41
 */
public abstract class BaseException extends RuntimeException {

    private final int code;
    private final String msg;

    protected BaseException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}


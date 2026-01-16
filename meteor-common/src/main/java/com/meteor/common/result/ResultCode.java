package com.meteor.common.result;

/**
 * 返回状态码
 *
 * @author Programmer
 * @date 2026-01-16 16:31
 */
public enum ResultCode {

    SUCCESS(200, "success"),
    FAIL(400, "fail"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
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


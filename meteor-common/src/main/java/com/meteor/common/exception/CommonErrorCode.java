package com.meteor.common.exception;

/**
 * 业务异常枚举
 *
 * @author Programmer
 * @date 2026-01-17 15:51
 */
public enum CommonErrorCode implements IErrorCode {

    // 通用
    SUCCESS(0, "成功"),
    PARAM_INVALID(400, "参数不合法"),
    UNAUTHORIZED(401, "未登录或登录失效"),
    FORBIDDEN(403, "无权限访问"),

    // 用户模块
    USER_EXIST(1001, "用户已存在"),
    USER_NOT_EXIST(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "用户名或密码错误"),
    ACCOUNT_DISABLED(1004, "账号已禁用");




    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}


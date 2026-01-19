package com.meteor.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务异常枚举
 *
 * @author Programmer
 * @date 2026-01-17 15:51
 */
public enum CommonErrorCode implements IErrorCode {

    // 通用
    SUCCESS(0, "成功", HttpStatus.OK),
    PARAM_INVALID(400, "参数不合法", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "未登录或登录失效", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "无权限访问", HttpStatus.FORBIDDEN),
    SYSTEM_ERROR(500, "系统异常", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(503, "服务不可用，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE),

    // 用户模块
    USER_EXIST(1001, "用户已存在", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST(1002, "用户不存在", HttpStatus.NOT_FOUND),
    PASSWORD_ERROR(1003, "用户名或密码错误", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(1004, "账号已禁用", HttpStatus.FORBIDDEN),
    PHONE_EXIST(1005, "手机号已存在", HttpStatus.BAD_REQUEST),

    // 文件上传模块
    FILE_UPLOAD_FAILED(2001, "文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED(2002, "文件删除失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_ERROR(2003,"文件大小超出限制" , HttpStatus.BAD_REQUEST),

    // 图片相关异常
    AVATAR_SIZE_ERROR(3001, "头像大小不能超过 2MB", HttpStatus.BAD_REQUEST),
    AVATAR_TYPE_ERROR(3002, "头像格式错误", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    CommonErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}


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
    SUCCESS(200, "成功", HttpStatus.OK),
    PARAM_INVALID(400, "参数不合法", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "未登录或登录失效", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "无权限访问", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "未找到该资源", HttpStatus.NOT_FOUND),
    SYSTEM_ERROR(500, "系统异常", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(503, "服务不可用，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE),
    PHONE_CODE_TOO_FREQUENT(400, "验证码发送过于频繁", HttpStatus.BAD_REQUEST),
    PHONE_CODE_REQUIRED(400, "验证码不能为空", HttpStatus.BAD_REQUEST),
    PHONE_CODE_ERROR(400, "验证码错误", HttpStatus.BAD_REQUEST),
    REQUEST_TOO_FREQUENT(400, "请求过于频繁", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(405, "不支持当前请求方法", HttpStatus.METHOD_NOT_ALLOWED),
    DATA_ERROR(400, "数据异常", HttpStatus.BAD_REQUEST),
    TIME_REVERSED(400, "时间逆转了，拒绝验证身份" , HttpStatus.BAD_REQUEST),


    // 用户模块
    USER_EXIST(1001, "用户已存在", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST(1002, "用户不存在", HttpStatus.NOT_FOUND),
    PASSWORD_ERROR(1003, "密码错误", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(1004, "账号已禁用", HttpStatus.FORBIDDEN),
    PHONE_EXIST(1005, "手机号已存在", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRM_ERROR(1006, "新密码与确认密码不一致", HttpStatus.BAD_REQUEST),
    USER_OR_PASSWORD_ERROR(1003, "用户名或密码错误", HttpStatus.UNAUTHORIZED),
    PHONE_FORMAT_ERROR(1007, "手机号格式错误", HttpStatus.BAD_REQUEST),
    UNKNOWN_ROLE_ERROR(1008, "未知角色", HttpStatus.BAD_REQUEST),
    OPERATION_FAILED(1009, "操作失败", HttpStatus.BAD_REQUEST),



    // 文件相关模块
    FILE_UPLOAD_FAILED(2001, "文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED(2002, "文件删除失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_ERROR(2003,"文件大小超出限制" , HttpStatus.BAD_REQUEST),
    FILE_URL_GENERATE_FAILED(2004, "文件访问地址生成失败", HttpStatus.INTERNAL_SERVER_ERROR),


    // 图片相关异常
    AVATAR_SIZE_ERROR(3001, "头像大小不能超过 2MB", HttpStatus.BAD_REQUEST),
    AVATAR_TYPE_ERROR(3002, "头像格式错误", HttpStatus.BAD_REQUEST),
    IMAGE_PROCESS_ERROR(3003, "图片处理失败", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_TYPE_ERROR(3004, "图片格式错误", HttpStatus.BAD_REQUEST),

    // 商家相关异常
    OPERATION_NOT_ALLOWED(5001, "请勿重复提交", HttpStatus.FORBIDDEN),
    MERCHANT_APPLY_NOT_EXIST(5002, "商家申请不存在", HttpStatus.NOT_FOUND),
    MERCHANT_APPLY_ALREADY_REVIEWED(5003, "该申请已审核，不能重复操作", HttpStatus.BAD_REQUEST),
    MERCHANT_APPLY_REJECT_REASON_REQUIRED(5004, "拒绝原因不能为空", HttpStatus.BAD_REQUEST),
    MERCHANT_NOT_EXIST(5005, "商家不存在", HttpStatus.NOT_FOUND),
    MERCHANT_STATUS_INVALID(5006, "商家状态无效", HttpStatus.BAD_REQUEST),


    //MQ
    INVALID_MQ_MESSAGE(6001, "消息体不合法", HttpStatus.BAD_REQUEST),
    MQ_SEND_FAILED(6002, "消息发送失败", HttpStatus.INTERNAL_SERVER_ERROR),

    // 票务相关
    PARAM_ERROR(7001, "参数错误", HttpStatus.BAD_REQUEST),

    // 电影相关异常
    CONFLICT(8001, "电影已存在", HttpStatus.BAD_REQUEST);

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


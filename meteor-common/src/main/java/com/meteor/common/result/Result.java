package com.meteor.common.result;

import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.exception.IErrorCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回体
 *
 * @author Programmer
 * @Date 2026-01-13 18:49
 */
@Data
@NoArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    public static <T> Result<T> success() {
        return new Result<>(
                CommonErrorCode.SUCCESS.getCode(),
                CommonErrorCode.SUCCESS.getMessage(),
                null
        );
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(
                CommonErrorCode.SUCCESS.getCode(),
                CommonErrorCode.SUCCESS.getMessage(),
                data
        );
    }

    public static <T> Result<T> fail(IErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMsg(errorCode.getMessage());
        result.setData(null);
        return result;
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }



    public static <T> Result<T> fail(CommonErrorCode errorCode, String message) {
        return new Result<>(
                errorCode.getCode(),
                message,
                null
        );
    }

    /**
     * 兜底失败返回（仅用于框架 / 非业务场景）
     * 业务代码禁止使用
     */
    public static <T> Result<T> fail(String message) {
        return fail(CommonErrorCode.SYSTEM_ERROR, message);
    }

}

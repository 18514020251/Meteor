package com.meteor.common.exception;

/**
 * 业务异常
 *
 * @author Programmer
 * @date 2026-01-16 16:47
 */
import com.meteor.common.result.ResultCode;

public class BizException extends BaseException {

    public BizException(String msg) {
        super(ResultCode.FAIL.getCode(), msg);
    }

    public BizException(ResultCode code) {
        super(code.getCode(), code.getMsg());
    }

    public BizException(int code, String msg) {
        super(code, msg);
    }
}


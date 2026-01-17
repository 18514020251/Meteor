package com.meteor.common.exception;

import com.meteor.common.result.ResultCode;

/**
 * 业务异常
 *
 * @author Programmer
 * @date 2026-01-17 15:32
 */
public class BizException extends BaseException {

    public BizException(String msg) {
        super(ResultCode.FAIL.getCode(), msg);
    }

}

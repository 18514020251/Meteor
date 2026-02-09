package com.meteor.ticketing.controller.vo;

import com.meteor.api.enums.GrabOrderResultEnum;

/**
 *  抢票结果VO
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 11:31
 */
public record GrabOrderVO(
        int code,
        String msg,
        String orderNo,
        Long leftStock
) {

    public static GrabOrderVO of(GrabOrderResultEnum e, String orderNo, Long leftStock) {
        return new GrabOrderVO(e.getCode(), e.getMsg(), orderNo, leftStock);
    }

    public static GrabOrderVO of(GrabOrderResultEnum e) {
        return new GrabOrderVO(e.getCode(), e.getMsg(), null, null);
    }
}

package com.meteor.order.controller.vo.pay;

/**
 *
 * 支付状态返回
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-10 10:25
 */
public record PayStatusVO(

        Integer status,

        String payNo,

        String orderNo

) {}

package com.meteor.order.controller.vo.pay;

import java.time.LocalDateTime;

/**
 *  创建支付单返回
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-10 10:24
 */
public record PayCreateVO(

        String payNo,

        Integer channel,

        String qrContent,

        LocalDateTime expireTime

) {}

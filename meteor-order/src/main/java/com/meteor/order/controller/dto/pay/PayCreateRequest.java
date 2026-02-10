package com.meteor.order.controller.dto.pay;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 *  创建支付单
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-10 10:26
 */
@Data
public class PayCreateRequest {

    @NotBlank(message = "orderNo不能为空")
    private String orderNo;

    /** 1=ALIPAY 2=WECHAT */
    @NotNull(message = "channel不能为空")
    private Integer channel;
}

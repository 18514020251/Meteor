package com.meteor.order.controller.dto.pay;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模拟支付确认请求
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-10 10:28
 */
@Data
public class PayConfirmRequest {
    @NotBlank(message = "payNo不能为空")
    private String payNo;

    /** 演示用支付密码 */
    @NotBlank(message = "payPwd不能为空")
    private String payPwd;

    @NotNull(message = "uid不能为空")
    private Long uid;
}

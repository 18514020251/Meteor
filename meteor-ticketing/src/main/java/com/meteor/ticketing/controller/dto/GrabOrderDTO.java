package com.meteor.ticketing.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *  抢单DTO
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-13
 */
@Data
public class GrabOrderDTO {

    @NotNull(message = "场次ID不能为空")
    @Min(value = 1, message = "场次ID必须大于0")
    private Long screeningId;

    @NotBlank(message = "客户端请求标识不能为空")
    @Size(max = 64, message = "客户端请求标识长度不能超过64")
    private String clientRequestId;
}

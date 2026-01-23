package com.meteor.admin.domain.dto;

import lombok.Data;

/**
 *  商家申请查询参数
 *
 * @author Programmer
 * @date 2026-01-23 17:11
 */
@Data
public class MerchantApplyQueryDTO {

    private Long userId;

    private String shopName;

    private Integer status;

    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

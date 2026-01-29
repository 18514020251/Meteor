package com.meteor.admin.domain.dto;

import com.meteor.common.constants.PageConstants;
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

    private Integer pageNum = PageConstants.DEFAULT_PAGE_NUM;
    private Integer pageSize = PageConstants.ADMIN_FIXED_PAGE_SIZE;
}

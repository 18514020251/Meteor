package com.meteor.admin.domain.dto;

import com.meteor.common.constants.PageConstants;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *  商家申请未发送查询条件
 *
 * @author Programmer
 * @date 2026-01-29 12:06
 */
@Data
public class MerchantApplyUnsentQueryDTO {
    private Integer pageNum = PageConstants.DEFAULT_PAGE_NUM;

    private Integer status;
    private Long userId;
    private String shopName;

    private LocalDateTime reviewedTimeStart;
    private LocalDateTime reviewedTimeEnd;
}
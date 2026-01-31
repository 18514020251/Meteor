package com.meteor.message.controller.dto;

import com.meteor.common.constants.PageConstants;
import lombok.Data;

/**
 *  获取消息DTO
 *
 * @author Programmer
 * @date 2026-01-29 17:48
 */
@Data
public class GetTheMessageDTO {

    private Integer readStatus;

    private Integer pageNum = PageConstants.DEFAULT_PAGE_NUM;

    private Integer pageSize = PageConstants.USER_FIXED_PAGE_SIZE;
}

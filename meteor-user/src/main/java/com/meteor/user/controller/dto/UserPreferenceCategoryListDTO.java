package com.meteor.user.controller.dto;

import lombok.Data;

import java.util.List;


/**
 *  用户手动选择喜好分类列表 DTO  远程调用
 *
 * @author Programmer
 * @date 2026-02-04 10:13
 */
@Data
public class UserPreferenceCategoryListDTO {

    /**
     * 是否已初始化偏好
     * 来自 user.preference_inited
     */
    private Boolean preferenceInited;

    /**
     * 用户喜好分类列表
     */
    private List<UserPreferenceCategorySummaryDTO> items;
}



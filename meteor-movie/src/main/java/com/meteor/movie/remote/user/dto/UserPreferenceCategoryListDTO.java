package com.meteor.movie.remote.user.dto;

import lombok.Data;

import java.util.List;

/**
 *  用户手动选择喜好分类列表 DTO
 *
 * @author Programmer
 * @date 2026-02-04 12:53
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

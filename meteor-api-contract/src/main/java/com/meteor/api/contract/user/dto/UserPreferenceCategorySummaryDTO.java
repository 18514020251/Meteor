package com.meteor.api.contract.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 *  用户手动选择喜好分类列表 DTO
 *
 * @author Programmer
 * @date 2026-02-04 12:53
 */
@Data
public class UserPreferenceCategorySummaryDTO {
    /**
     * 分类ID（movie模块的分类）
     */
    private Long categoryId;

    /**
     * 偏好分数（直接当权重用）
     */
    private Integer score;

    /**
     * 最近触达时间（用于同分排序稳定）
     */
    private LocalDateTime lastTouchTime;
}

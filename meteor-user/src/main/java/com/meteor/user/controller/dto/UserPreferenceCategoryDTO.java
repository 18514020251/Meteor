package com.meteor.user.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 *  用户手动选择喜好分类请求 DTO
 *
 * @author Programmer
 * @date 2026-02-03 20:57
 */
@Data
@Schema(description = "用户手动选择喜好分类请求")
public class UserPreferenceCategoryDTO {

    @NotEmpty(message = "请选择至少一个分类")
    @Size(max = 4, message = "一次最多选择 4 个分类")
    @Schema(description = "分类ID列表(来自movie模块)", example = "[1,2,3]")
    private List<Long> categoryIds;
}

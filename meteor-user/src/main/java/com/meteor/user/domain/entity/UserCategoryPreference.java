package com.meteor.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户分类偏好(多来源)
 * </p>
 *
 * @author Programmer
 * @since 2026-02-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_category_preference")
@Schema(description="用户分类偏好(多来源)")
public class UserCategoryPreference implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "分类ID(来自movie模块)")
    private Long categoryId;

    @Schema(description = "来源:1=manual 2=purchase 3=browse")
    private Integer source;

    @Schema(description = "偏好分数(越大越喜欢)")
    private Integer score;

    @Schema(description = "最后一次触达时间(购买/浏览/手动更新)")
    private LocalDateTime lastSeenTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}

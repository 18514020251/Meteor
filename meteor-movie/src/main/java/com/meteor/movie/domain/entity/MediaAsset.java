package com.meteor.movie.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.movie.enums.MediaAssetKindEnum;
import com.meteor.movie.enums.MediaBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 图片资源表
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("media_asset")
@Schema(description="MediaAsset对象")
public class MediaAsset implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "资源ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "1=movie 2=screening 3=cinema")
    private MediaBizTypeEnum bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "图片URL")
    private String url;

    @Schema(description = "1=POSTER 2=COVER 3=GALLERY")
    private MediaAssetKindEnum kind;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "是否删除 0=否 1=是")
    private DeleteStatus deleted;
}

package com.meteor.movie.domain.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.movie.enums.MovieStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 电影信息表
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("movie")
@Schema(description="Movie对象")
public class Movie implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "电影ID")
    @TableId( type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "电影名称")
    private String title;

    @Schema(description = "别名/英文名")
    private String alias;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "时长(分钟)")
    private Integer durationMin;

    @Schema(description = "上映日期")
    private LocalDate releaseDate;

    @Schema(description = "1=COMING 2=SHOWING 3=OFF")
    private MovieStatusEnum status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "是否删除 0=否 1=是")
    @TableLogic
    private DeleteStatus deleted;
}

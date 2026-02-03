package com.meteor.movie.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 新增电影 DTO
 *
 * @author Programmer
 */
@Data
@Schema(description = "新增电影请求")
public class MovieCreateDTO {

    @Schema(description = "电影名称", example = "流星电影院：代码能跑就别动")
    @NotBlank(message = "电影名称不能为空")
    @Size(max = 128, message = "电影名称长度不能超过 128")
    private String title;

    @Schema(description = "别名/英文名", example = "Meteor Cinema: It Works")
    @Size(max = 128, message = "别名长度不能超过 128")
    private String alias;

    @Schema(description = "简介", example = "一部关于Java开发者与Bug互相成就的纪录片。")
    private String intro;

    @Schema(description = "时长(分钟)", example = "128")
    @Min(value = 1, message = "时长必须大于 0")
    @Max(value = 1000, message = "时长不合理")
    private Integer durationMin;

    @Schema(description = "上映日期", example = "2026-02-02")
    private LocalDate releaseDate;

    @Schema(description = "分类ID列表", example = "[1,3,7]")
    @NotEmpty(message = "至少选择一个电影类型")
    @Size(max = 3, message = "电影类型最多选择 3 个")
    private List<@NotNull(message = "分类ID不能为空") Long> categoryIds;


    @Schema(description = "海报URL(kind=POSTER)", example = "xxx/poster.png")
    @Size(max = 512, message = "posterUrl 太长")
    private String posterKey;

    @Schema(description = "封面URL(kind=COVER)", example = "xxx/cover.png")
    @Size(max = 512, message = "coverUrl 太长")
    private String coverKey;

    @Schema(description = "图集URL列表(kind=GALLERY)，顺序即sort", example = "[\"xxx/1.png\",\"xxx/2.png\"]")
    @Size(max = 9, message = "图集最多 9 张")
    private List<
            @NotBlank(message = "galleryKey不能为空")
            @Size(max = 512, message = "galleryKey 太长")
                    String
            > galleryKeys;

}


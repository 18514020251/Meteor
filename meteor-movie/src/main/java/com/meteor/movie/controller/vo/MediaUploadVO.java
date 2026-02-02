package com.meteor.movie.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *  媒体资源上传响应 VO
 *
 * @author Programmer
 * @date 2026-02-02 18:23
 */
@Data
@Schema(description = "媒体资源上传响应")
public class MediaUploadVO {

    @Schema(
            description = "对象Key（用于写库，唯一可信标识）",
            example = "movie/gallery/20260202/3f8a2c9e.png"
    )
    private String objectKey;

    @Schema(
            description = "临时预览URL（预签名生成，不可存入数据库）",
            example = "https://minio.xxx.com/bucket/movie/gallery/20260202/3f8a2c9e.png?X-Amz-Expires=300"
    )
    private String previewUrl;
}

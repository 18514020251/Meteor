package com.meteor.movie.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;


/**
 * @author Programmer
 * @date 2026-02-02 16:31
 */

@Getter
@Schema(description = "媒体资源类型")
public enum MediaAssetKindEnum {

    POSTER(1, "poster", "海报"),
    COVER(2, "cover", "封面"),
    GALLERY(3, "gallery", "图集");

    @EnumValue
    private final int code;

    private final String dir;

    private final String desc;

    MediaAssetKindEnum(int code, String dir, String desc) {
        this.code = code;
        this.dir = dir;
        this.desc = desc;
    }
}


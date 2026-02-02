package com.meteor.movie.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author Programmer
 * @date 2026-02-02 16:31
 */

@Getter
@Schema(description = "媒体资源类型")
public enum MediaAssetKindEnum {

    POSTER(1, "海报"),
    COVER(2, "封面"),
    GALLERY(3, "图集");

    @EnumValue
    private final int code;
    private final String desc;

    MediaAssetKindEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MediaAssetKindEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("非法的 MediaAssetKindEnum code: " + code));
    }

    public static boolean isValid(Integer code) {
        if (code == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(e -> e.code == code);
    }
}

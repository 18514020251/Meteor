package com.meteor.movie.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 *  媒体资源业务类型枚举
 *
 * @author Programmer
 * @date 2026-02-02 17:02
 */
@Getter
@Schema(description = "媒体资源业务类型")
public enum MediaBizTypeEnum {

    MOVIE(1, "电影"),
    SCREENING(2, "场次"),
    CINEMA(3, "影院");

    @EnumValue
    private final int code;

    private final String desc;

    MediaBizTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
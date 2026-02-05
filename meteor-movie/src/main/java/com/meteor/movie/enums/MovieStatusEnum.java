package com.meteor.movie.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Arrays;

/**
 *  电影状态枚举
 *
 * @author Programmer
 * @date 2026-02-02 16:34
 */
@Getter
@Schema(description = "电影状态")
public enum MovieStatusEnum {

    COMING(1, "未上映"),
    SHOWING(2, "上映中"),
    OFF(3, "已下线");

    @EnumValue
    private final int code;
    private final String desc;

    MovieStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MovieStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("非法的 MovieStatusEnum code: " + code));
    }

    public static boolean isValid(Integer code) {
        if (code == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(e -> e.code == code);
    }
}

package com.meteor.user.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 *  用户-角色枚举类
 *
 * @author Programmer
 * @date 2026-01-13 18:42
 * <p>
 * 仅用于业务域/DB映射，不用于 Sa-Token 鉴权。
 * Sa-Token 角色常量统一使用 RoleConst。
 * </p>
 */
@Getter
public enum RoleEnum {

    USER(0, "user"),
    MERCHANT(1, "merchant"),
    ADMIN(2, "admin");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String desc;

    RoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RoleEnum fromCode(Integer code) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }

}

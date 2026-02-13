package com.meteor.common.enums.system;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 *  系统枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 11:32
 */
@Getter
public enum ModuleEnum {

    APPLY("apply"),
    MERCHANT("merchant"),
    USER("user");

    @EnumValue
    private final String moduleName;

    ModuleEnum(String moduleName) {
        this.moduleName = moduleName;
    }

    public static ModuleEnum fromModuleName(String moduleName) {
        for (ModuleEnum module : values()) {
            if (module.getModuleName().equalsIgnoreCase(moduleName)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Unknown module name: " + moduleName);
    }
}

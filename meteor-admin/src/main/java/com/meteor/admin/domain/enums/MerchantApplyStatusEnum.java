package com.meteor.admin.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 *  商家申请状态枚举
 *
 * @author Programmer
 * @date 2026-01-23 16:42
 */
@Getter
public enum MerchantApplyStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "拒绝");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String desc;


    MerchantApplyStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MerchantApplyStatusEnum fromCode(Integer status) {
        if (status == null) {
            return null;
        }
        for (MerchantApplyStatusEnum value : values()) {
            if (value.getCode().equals(status)) {
                return value;
            }
        }
        return null;
    }

}

package com.meteor.common.enums.merchant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 商家申请状态枚举
 *
 * @author Programmer
 */
@Getter
public enum MerchantApplyStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");


    @EnumValue
    private final Integer code;

    private final String desc;


    MerchantApplyStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MerchantApplyStatusEnum fromCode(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("MerchantApplyStatus code is null");
        }
        for (MerchantApplyStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown MerchantApplyStatus code: " + code);
    }
}

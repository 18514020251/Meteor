package com.meteor.admin.domain.enums;

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

    private final int code;
    private final String desc;

    MerchantApplyStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

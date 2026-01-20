package com.meteor.user.domain.merchant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商家申请状态枚举
 *
 * @author Programmer
 */
@Getter
@AllArgsConstructor
public enum MerchantApplyStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final Integer code;
    private final String desc;
}

package com.meteor.analytics.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统计范围枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 17:40
 */
@Getter
@AllArgsConstructor
public enum BizScopeEnum {

    GLOBAL("GLOBAL", 0L),

    MERCHANT("MERCHANT", null);

    @EnumValue
    private final String scope;
    private final Long defaultBizId;

}

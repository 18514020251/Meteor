package com.meteor.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  业务类型枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 22:14
 */
@Getter
@AllArgsConstructor
public enum OrderBizTypeEnum {

    MOVIE_TICKET(1, "电影票");

    @EnumValue
    private final int code;
    private final String desc;
}

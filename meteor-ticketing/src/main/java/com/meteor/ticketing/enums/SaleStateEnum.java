package com.meteor.ticketing.enums;

import lombok.Getter;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-08 11:34
 */
@Getter
public enum SaleStateEnum {

    NOT_STARTED("未开售"),
    SELLING("可购买"),
    SOLD_OUT("已售罄"),
    CLOSED("已停售"),
    STOPPED("已停售"),
    CANCELED("已取消");

    private final String desc;

    SaleStateEnum(String desc) {
        this.desc = desc;
    }
}

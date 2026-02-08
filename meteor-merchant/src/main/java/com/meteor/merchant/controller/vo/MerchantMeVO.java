package com.meteor.merchant.controller.vo;

import com.meteor.merchant.enums.MerchantStatusEnum;

import java.time.LocalDateTime;

/**
 *  商家信息 VO
 *
 * @author Programmer
 * @date 2026-02-01 11:19
 */
public record MerchantMeVO(

    Long merchantId,
    Long userId,
    String shopName,
    String notice,
    MerchantStatusEnum status,
    LocalDateTime verifiedTime,

    String username,
    String phone,
    String avatar
){}

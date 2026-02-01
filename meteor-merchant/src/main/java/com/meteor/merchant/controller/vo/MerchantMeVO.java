package com.meteor.merchant.controller.vo;

import com.meteor.merchant.enums.MerchantStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *  商家信息 VO
 *
 * @author Programmer
 * @date 2026-02-01 11:19
 */
@Data
public class MerchantMeVO {

    private Long merchantId;
    private Long userId;
    private String shopName;
    private String notice;
    private MerchantStatusEnum status;
    private LocalDateTime verifiedTime;

    private String username;
    private String phone;
    private String avatar;
}

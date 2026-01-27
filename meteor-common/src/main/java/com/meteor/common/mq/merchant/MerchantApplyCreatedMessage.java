package com.meteor.common.mq.merchant;

import lombok.Data;

import java.time.LocalDateTime;


/**
 * 商家申请创建消息
 * @author Programmer
 * @date 2026-01-21 16:43
 */
@Data
public class MerchantApplyCreatedMessage {

    /** 用户侧申请ID（user.merchant_apply.id） */
    private Long applyId;

    /** 用户ID */
    private Long userId;

    /** 店铺名称 */
    private String shopName;

    /** 申请理由 */
    private String applyReason;

    /** 申请时间 */
    private LocalDateTime applyTime;
}



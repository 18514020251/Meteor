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

    private Long applyId;

    private Long userId;

    private String shopName;

    private String applyReason;

    private LocalDateTime applyTime;
}



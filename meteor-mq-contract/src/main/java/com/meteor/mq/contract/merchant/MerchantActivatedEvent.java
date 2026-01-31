package com.meteor.mq.contract.merchant;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *  商家激活消息
 *
 * @author Programmer
 * @date 2026-01-31 17:45
 */
@Data
public class MerchantActivatedEvent implements Serializable {

    /** 全局事件ID（Snowflake，用于幂等/追踪） */
    private Long eventId;

    /** 商家申请ID（merchant_apply.id，用于业务幂等） */
    private Long applyId;

    /** 用户ID（唯一定位商家） */
    private Long userId;

    /** 店铺名称（来自申请） */
    private String shopName;

    /** 审核通过/激活时间 */
    private LocalDateTime activatedAt;
}

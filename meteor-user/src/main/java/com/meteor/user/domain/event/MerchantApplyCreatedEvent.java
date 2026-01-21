package com.meteor.user.domain.event;

import com.meteor.user.domain.entity.MerchantApply;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *  商家申请创建事件
 *
 * @author Programmer
 */
@Data
public class MerchantApplyCreatedEvent implements Serializable {

    private Long applyId;
    private Long userId;
    private String shopName;
    private LocalDateTime createTime;

    public static MerchantApplyCreatedEvent of(MerchantApply apply) {
        MerchantApplyCreatedEvent event = new MerchantApplyCreatedEvent();
        event.setApplyId(apply.getId());
        event.setUserId(apply.getUserId());
        event.setShopName(apply.getShopName());
        event.setCreateTime(apply.getCreateTime());
        return event;
    }
}

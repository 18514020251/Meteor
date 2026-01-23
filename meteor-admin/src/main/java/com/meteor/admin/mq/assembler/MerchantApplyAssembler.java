package com.meteor.admin.mq.assembler;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.domain.enums.MerchantApplyStatusEnum;
import com.meteor.common.mq.merchant.MerchantApplyCreatedMessage;
import org.springframework.stereotype.Component;

/**
 *  商家申请消息队列转换
 *
 * @author Programmer
 * @date 2026-01-23 16:48
 */
@Component
public class MerchantApplyAssembler {

    public MerchantApply from(MerchantApplyCreatedMessage message) {
        MerchantApply entity = new MerchantApply();
        entity.setApplyId(message.getApplyId());
        entity.setUserId(message.getUserId());
        entity.setShopName(message.getShopName());
        entity.setApplyReason(message.getApplyReason());
        entity.setStatus(MerchantApplyStatusEnum.PENDING);
        entity.setCreateTime(message.getApplyTime());
        return entity;
    }
}

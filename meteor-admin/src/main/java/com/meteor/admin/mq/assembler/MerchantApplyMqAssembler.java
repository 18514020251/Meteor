package com.meteor.admin.mq.assembler;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.common.mq.merchant.MerchantApplyCreatedMessage;
import com.meteor.common.mq.merchant.MerchantApplyReviewedMessage;
import org.springframework.stereotype.Component;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;

/**
 *  商家申请消息队列转换
 *
 * @author Programmer
 * @date 2026-01-23 16:48
 */
@Component
public class MerchantApplyMqAssembler {

    /**
     * <p>
     *      转换成商家申请
     * </p>
     * @param message 商家申请创建消息
     * */
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


    public MerchantApplyReviewedMessage toReviewedMessage(MerchantApply apply ){
        MerchantApplyReviewedMessage message = new MerchantApplyReviewedMessage();
        message.setApplyId(apply.getApplyId());
        message.setStatus(MerchantApplyStatusEnum.fromCode(apply.getStatus()));
        message.setReviewedBy(apply.getReviewedBy());
        message.setReviewedTime(apply.getReviewedTime());
        message.setRejectReason(apply.getRejectReason());
        message.setUserId(apply.getUserId());
        return message;
    }
}

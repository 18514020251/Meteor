package com.meteor.admin.mq.assembler;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.mq.contract.enums.merchant.MerchantApplyStatus;
import com.meteor.mq.contract.merchant.MerchantApplyCreatedMessage;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import org.springframework.stereotype.Component;

/**
 * 商家申请消息队列转换
 *
 * @author Programmer
 * @date 2026-01-23 16:48
 */
@Component
public class MerchantApplyMqAssembler {

    /**
     * 转换成商家申请（admin库落表）
     */
    public MerchantApply from(MerchantApplyCreatedMessage message) {
        MerchantApply entity = new MerchantApply();
        entity.setApplyId(message.getApplyId());
        entity.setUserId(message.getUserId());
        entity.setShopName(message.getShopName());
        entity.setApplyReason(message.getApplyReason());

        entity.setStatus(MerchantApplyStatusEnum.fromCode(MerchantApplyStatusEnum.PENDING.getCode()));

        entity.setCreateTime(message.getApplyTime());
        return entity;
    }

    /**
     * admin -> user：审核结果消息
     */
    public MerchantApplyReviewedMessage toReviewedMessage(MerchantApply apply) {
        MerchantApplyReviewedMessage message = new MerchantApplyReviewedMessage();
        message.setApplyId(apply.getApplyId());

        MerchantApplyStatusEnum bizStatus = MerchantApplyStatusEnum.fromCode(apply.getStatus());
        if (bizStatus == null) {
            throw new IllegalArgumentException("Unknown MerchantApplyStatus code: " + apply.getStatus());
        }

        message.setStatus(MerchantApplyStatus.valueOf(bizStatus.name()));

        message.setReviewedBy(apply.getReviewedBy());
        message.setReviewedTime(apply.getReviewedTime());
        message.setRejectReason(apply.getRejectReason());
        message.setUserId(apply.getUserId());
        return message;
    }
}

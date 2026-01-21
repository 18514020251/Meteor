package com.meteor.user.mq.assembler;

import com.meteor.common.mq.merchant.MerchantApplyCreatedMessage;
import com.meteor.user.domain.entity.MerchantApply;
import org.springframework.stereotype.Component;

/**
 * 商家申请 MQ 消息构造器
 * @author Programmer
 */
@Component
public class MerchantApplyMessageAssembler {

    public MerchantApplyCreatedMessage from(MerchantApply apply) {
        MerchantApplyCreatedMessage message = new MerchantApplyCreatedMessage();
        message.setApplyId(apply.getId());
        message.setUserId(apply.getUserId());
        message.setShopName(apply.getShopName());
        message.setApplyReason(apply.getApplyReason());
        message.setApplyTime(apply.getCreateTime());
        return message;
    }
}

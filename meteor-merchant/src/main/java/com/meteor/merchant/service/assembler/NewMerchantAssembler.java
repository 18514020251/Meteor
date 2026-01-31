package com.meteor.merchant.service.assembler;

import com.meteor.merchant.domain.entity.Merchant;
import com.meteor.merchant.enums.MerchantStatusEnum;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *  新商家 assembler
 *
 * @author Programmer
 * @date 2026-01-31 23:08
 */
@Component
public class NewMerchantAssembler {

    public Merchant buildNewMerchant(
            MerchantApplyReviewedMessage msg,
            LocalDateTime verifiedTime
    ) {
        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant();
        merchant.setUserId(msg.getUserId());
        merchant.setShopName(msg.getShopName());
        merchant.setStatus(MerchantStatusEnum.NORMAL);
        merchant.setVerifiedTime(verifiedTime);
        merchant.setApplyId(msg.getApplyId());
        merchant.setCreateTime(now);
        merchant.setUpdateTime(now);
        return merchant;
    }
}

package com.meteor.admin.service.assembler;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.controller.vo.MerchantApplyUnsentVO;
import org.springframework.stereotype.Component;

/**
 *  商家申请assembler
 *
 * @author Programmer
 * @date 2026-01-29 12:25
 */
@Component
public class MerchantApplyVoAssembler {

    public MerchantApplyUnsentVO toUnsentVO(MerchantApply a) {
        return new MerchantApplyUnsentVO(
                a.getApplyId(),
                a.getUserId(),
                a.getShopName(),
                a.getStatus().getCode(),
                a.getReviewedBy(),
                a.getReviewedTime(),
                a.getReviewedMsgSent(),
                a.getReviewedMsgSentTime(),
                a.getRejectReason()
        );
    }
}

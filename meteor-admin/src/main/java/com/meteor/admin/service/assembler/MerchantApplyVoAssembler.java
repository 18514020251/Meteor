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
        MerchantApplyUnsentVO vo = new MerchantApplyUnsentVO();
        vo.setApplyId(a.getApplyId());
        vo.setUserId(a.getUserId());
        vo.setShopName(a.getShopName());
        vo.setStatus(a.getStatus().getCode());
        vo.setReviewedBy(a.getReviewedBy());
        vo.setReviewedTime(a.getReviewedTime());
        vo.setReviewedMsgSent(a.getReviewedMsgSent());
        vo.setReviewedMsgSentTime(a.getReviewedMsgSentTime());
        vo.setRejectReason(a.getRejectReason());
        return vo;
    }
}

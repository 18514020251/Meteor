package com.meteor.admin.service;

import com.meteor.admin.controller.vo.ReviewedResendVO;

/**
 *  商家申请审核结果补偿服务
 *
 * @author Programmer
 * @date 2026-01-29 14:25
 */
public interface IMerchantApplyReviewedCompensateService {
    /**
     *  重发审核结果
     * */
    ReviewedResendVO resendByApplyId(Long applyId);
}

package com.meteor.admin.service;

import com.meteor.admin.controller.vo.ReviewedResendVO;

/**
 * 商家申请审核结果补偿服务接口。
 * <p>
 * 用于对已完成审核但审核结果消息未成功发送的商家申请，
 * 提供人工或系统触发的补发能力，确保审核结果最终一致性。
 * </p>
 *
 * @author Programmer
 * @date 2026-01-29 14:25
 */
public interface IMerchantApplyReviewedCompensateService {

    /**
     * 根据商家申请 ID 重新发送审核结果消息。
     * <p>
     * 当审核结果消息因异常原因未成功发送时，
     * 可通过该方法触发补发逻辑。
     * 若申请不存在或状态不允许补发，将抛出业务异常。
     * </p>
     *
     * @param applyId 商家申请 ID
     * @return 补发结果信息，包含补发状态及相关提示
     */
    ReviewedResendVO resendByApplyId(Long applyId);

}

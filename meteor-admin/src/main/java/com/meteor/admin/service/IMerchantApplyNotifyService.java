package com.meteor.admin.service;

import com.meteor.admin.domain.entity.MerchantApply;

/**
 *  商家申请审核结果通知服务
 *
 * @author Programmer
 * @date 2026-01-30 21:44
 */
public interface IMerchantApplyNotifyService {
    /**
     * 审核通过通知（发给 message 模块）
     */
    void notifyApproved(MerchantApply apply);

    /**
     * 审核拒绝通知（发给 message 模块）
     */
    void notifyRejected(MerchantApply apply);
}

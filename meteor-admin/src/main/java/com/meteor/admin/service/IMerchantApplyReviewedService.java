package com.meteor.admin.service;

import com.meteor.admin.domain.entity.MerchantApply;

/**
 *  发送逻辑服务
 *
 * @author Programmer
 * @date 2026-01-27 18:07
 */
public interface IMerchantApplyReviewedService {
    void send(MerchantApply apply);

    void send(MerchantApply apply , Runnable onSuccess);

}

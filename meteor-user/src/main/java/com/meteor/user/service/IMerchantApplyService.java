package com.meteor.user.service;

import com.meteor.user.controller.dto.MerchantApplyDTO;

/**
 *  商家申请表服务接口
 *
 * @author Programmer
 * @date 2026-01-20 23:35
 */

public interface IMerchantApplyService {

    void apply(Long userId, MerchantApplyDTO applyReason);
}

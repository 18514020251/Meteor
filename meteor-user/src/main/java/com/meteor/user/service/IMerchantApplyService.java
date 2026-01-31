package com.meteor.user.service;

import com.meteor.user.controller.dto.MerchantApplyDTO;

/**
 * 商家申请服务接口。
 * <p>
 * 用于处理用户申请成为商家的业务流程，
 * 包括申请信息校验、申请记录创建以及后续业务触发等操作。
 * </p>
 *
 * @author Programmer
 * @date 2026-01-20 23:35
 */
public interface IMerchantApplyService {

    /**
     * 提交商家申请。
     * <p>
     * 当前用户通过该方法提交商家申请信息，
     * 系统将根据业务规则创建申请记录并进入审核流程。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param applyReason 商家申请信息（包含店铺名称、申请理由等）
     */
    void apply(Long userId, MerchantApplyDTO applyReason);
}

package com.meteor.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meteor.admin.controller.dto.MerchantApplyUnsentQueryDTO;
import com.meteor.admin.controller.vo.MerchantApplyUnsentVO;

/**
 *  商家申请审核查询服务
 *
 * @author Programmer
 * @date 2026-01-29 12:11
 */
public interface IMerchantApplyReviewedQueryService {
    /**
     *  查询待审核的申请
     * */
    IPage<MerchantApplyUnsentVO> listUnsent(MerchantApplyUnsentQueryDTO dto);

}

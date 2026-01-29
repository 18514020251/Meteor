package com.meteor.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meteor.admin.domain.dto.MerchantApplyUnsentQueryDTO;
import com.meteor.admin.domain.vo.MerchantApplyUnsentVO;

/**
 * @author Programmer
 * @date 2026-01-29 12:11
 */
public interface IMerchantApplyReviewedQueryService {
    /**
     *  查询待审核的申请
     * */
    IPage<MerchantApplyUnsentVO> listUnsent(MerchantApplyUnsentQueryDTO dto);
}

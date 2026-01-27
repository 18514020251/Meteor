package com.meteor.admin.service;

import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.domain.entity.MerchantApply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.common.domain.PageResult;
import jakarta.validation.constraints.NotBlank;

/**
 * <p>
 * 商家申请表（管理端，审核视图） 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-23
 */
public interface IMerchantApplyService extends IService<MerchantApply> {

    PageResult<MerchantApplyDTO> list(MerchantApplyQueryDTO query);

    void approve(Long id);

    void reject(Long id, @NotBlank(message = "拒绝原因不能为空") String rejectReason);
}

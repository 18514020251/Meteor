package com.meteor.admin.service;

import com.meteor.admin.controller.dto.MerchantApplyDTO;
import com.meteor.admin.controller.dto.MerchantApplyQueryDTO;
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

    /**
     * <p>分页查询商家申请列表</p>
     * @param query 查询参数
     * @return 商家申请列表
     * */
    PageResult<MerchantApplyDTO> list(MerchantApplyQueryDTO query);

    /**
     * <p>审核通过</p>
     * @param id 商家申请ID
     * */
    void approveByApplyId(Long id);


    /**
     * <p>审核拒绝</p>
     * @param applyId 商家申请ID
     * @param rejectReason 拒绝原因
     * */
    void rejectByApplyId(Long applyId, @NotBlank(message = "拒绝原因不能为空") String rejectReason);

}

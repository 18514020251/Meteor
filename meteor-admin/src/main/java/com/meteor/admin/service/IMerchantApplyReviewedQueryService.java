package com.meteor.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meteor.admin.controller.dto.MerchantApplyUnsentQueryDTO;
import com.meteor.admin.controller.vo.MerchantApplyUnsentVO;

/**
 * 商家申请审核查询服务接口。
 * <p>
 * 提供对商家申请审核记录的查询能力，
 * 主要用于查询尚未发送审核结果消息的申请数据，
 * 支持分页查询。
 * </p>
 *
 * @author Programmer
 * @date 2026-01-29 12:11
 */
public interface IMerchantApplyReviewedQueryService {

    /**
     * 分页查询未发送审核结果消息的商家申请记录。
     * <p>
     * 根据查询条件获取当前待处理或未补发审核结果消息的
     * 商家申请列表，结果以分页形式返回。
     * </p>
     *
     * @param dto 查询条件对象，包含分页参数及筛选条件
     * @return 未发送审核结果消息的商家申请分页列表
     */
    IPage<MerchantApplyUnsentVO> listUnsent(MerchantApplyUnsentQueryDTO dto);

}

package com.meteor.admin.mapper;

import com.meteor.admin.domain.entity.MerchantApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 商家申请表（管理端，审核视图） Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-01-23
 */
public interface MerchantApplyMapper extends BaseMapper<MerchantApply> {

    @Select("select exists(select 1 from merchant_apply where apply_id = #{applyId})")
    boolean existsByApplyId(Long applyId);

}

package com.meteor.admin.repository.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.domain.entity.MerchantApply;

/**
 *  商家申请查询构造器
 *
 * @author Programmer
 * @date 2026-01-25 16:17
 */
public final class MerchantApplyQueryBuilder {

    private MerchantApplyQueryBuilder() {}

    public static LambdaQueryWrapper<MerchantApply> build(MerchantApplyQueryDTO query) {
        LambdaQueryWrapper<MerchantApply> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(query.getUserId() != null,
                MerchantApply::getUserId, query.getUserId());

        wrapper.eq(query.getStatus() != null,
                MerchantApply::getStatus, query.getStatus());

        wrapper.like(isNotBlank(query.getShopName()),
                MerchantApply::getShopName, query.getShopName());

        return wrapper;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}

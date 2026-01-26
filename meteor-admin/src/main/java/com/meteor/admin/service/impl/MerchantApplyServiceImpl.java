package com.meteor.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.admin.service.IMerchantApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * <p>
 * 商家申请表（管理端，审核视图） 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-23
 */
@Service
@RequiredArgsConstructor
public class MerchantApplyServiceImpl extends ServiceImpl<MerchantApplyMapper, MerchantApply> implements IMerchantApplyService {

    private final MerchantApplyMapper merchantApplyMapper;


    @Override
    public PageResult<MerchantApplyDTO> list(MerchantApplyQueryDTO query) {
        Page<MerchantApply> page = new Page<>(query.getPageNum() , query.getPageSize());

        LambdaQueryWrapper<MerchantApply> wrapper = buildWrapper(query);

        merchantApplyMapper.selectPage(page , wrapper);

        List<MerchantApplyDTO> dtoList = page.getRecords().stream()
                .map(MerchantApplyDTO::fromEntity)
                .toList();

        return PageResult.of(dtoList, page.getTotal() , query.getPageNum() , query.getPageSize());
    }

    /*
    *  构建查询条件
    * */
    private LambdaQueryWrapper<MerchantApply> buildWrapper(MerchantApplyQueryDTO query) {
        LambdaQueryWrapper<MerchantApply> wrapper = new LambdaQueryWrapper<>();
        if (query.getUserId() != null) {
            wrapper.eq(MerchantApply::getUserId, query.getUserId());
        }
        if (query.getShopName() != null && !query.getShopName().isBlank()) {
            wrapper.like(MerchantApply::getShopName, query.getShopName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(MerchantApply::getStatus, query.getStatus());
        }
        return wrapper;
    }
}

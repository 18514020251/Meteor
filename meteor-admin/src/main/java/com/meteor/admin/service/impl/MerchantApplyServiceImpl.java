package com.meteor.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.domain.es.MerchantApplyES;
import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.admin.service.IMerchantApplyService;
import com.meteor.admin.service.es.MerchantApplyESService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


/**
 * <p>
 * 商家申请表（管理端，审核视图） 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantApplyServiceImpl extends ServiceImpl<MerchantApplyMapper, MerchantApply> implements IMerchantApplyService {

    private final MerchantApplyMapper merchantApplyMapper;
    private final MerchantApplyESService merchantApplyESService;


    @Override
    public PageResult<MerchantApplyDTO> list(MerchantApplyQueryDTO query) {
        try {
            merchantApplyESService.initIndex();
            PageResult<MerchantApplyES> esPage = merchantApplyESService.searchPage(query);

            List<MerchantApplyDTO> dtoList = esPage.getRecords().stream()
                    .map(merchantApplyESService::toDTO)
                    .toList();

            return PageResult.of(
                    dtoList,
                    esPage.getTotal(),
                    query.getPageNum(),
                    query.getPageSize()
            );
        } catch (IOException e) {
            log.error("ES查询失败", e);
            return fallbackToList(query);
        }
    }


    /**
     * 降级到数据库查询
     */
    private PageResult<MerchantApplyDTO> fallbackToList(MerchantApplyQueryDTO query) {
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

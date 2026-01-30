package com.meteor.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meteor.admin.domain.assembler.MerchantApplyVoAssembler;
import com.meteor.admin.domain.dto.MerchantApplyUnsentQueryDTO;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.domain.vo.MerchantApplyUnsentVO;
import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.admin.service.IMerchantApplyReviewedQueryService;
import com.meteor.common.constants.PageConstants;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.mq.contract.enums.MessageSendStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * unsent 列表查询
 *
 * @author Programmer
 * @date 2026-01-29 12:12
 */
@Service
@RequiredArgsConstructor
public class MerchantApplyReviewedQueryServiceImpl implements IMerchantApplyReviewedQueryService {

    private final MerchantApplyMapper merchantApplyMapper;

    private final MerchantApplyVoAssembler assembler;

    @Override
    public IPage<MerchantApplyUnsentVO> listUnsent(MerchantApplyUnsentQueryDTO dto) {
        Page<MerchantApply> page = buildPage(dto);
        LambdaQueryWrapper<MerchantApply> qw = buildUnsentBaseQw();
        applyFilters(qw, dto);

        return merchantApplyMapper
                .selectPage(page, qw)
                .convert(assembler::toUnsentVO);
    }

    private Page<MerchantApply> buildPage(MerchantApplyUnsentQueryDTO dto) {
        return new Page<>(dto.getPageNum(), PageConstants.ADMIN_FIXED_PAGE_SIZE);
    }

    private void applyFilters(LambdaQueryWrapper<MerchantApply> qw, MerchantApplyUnsentQueryDTO dto) {
        if (dto == null) {
            return;
        }
        if (dto.getStatus() != null) {
            qw.eq(MerchantApply::getStatus, dto.getStatus());
        }
        if (dto.getUserId() != null) {
            qw.eq(MerchantApply::getUserId, dto.getUserId());
        }
        if (dto.getShopName() != null && !dto.getShopName().isBlank()) {
            qw.like(MerchantApply::getShopName, dto.getShopName().trim());
        }
        if (dto.getReviewedTimeStart() != null) {
            qw.ge(MerchantApply::getReviewedTime, dto.getReviewedTimeStart());
        }
        if (dto.getReviewedTimeEnd() != null) {
            qw.le(MerchantApply::getReviewedTime, dto.getReviewedTimeEnd());
        }
    }

    private LambdaQueryWrapper<MerchantApply> buildUnsentBaseQw() {
        return new LambdaQueryWrapper<MerchantApply>()
                .eq(MerchantApply::getReviewedMsgSent, MessageSendStatusEnum.UNSENT.getCode())
                .in(MerchantApply::getStatus,
                        MerchantApplyStatusEnum.APPROVED.getCode(),
                        MerchantApplyStatusEnum.REJECTED.getCode())
                .orderByDesc(MerchantApply::getReviewedTime);
    }

}
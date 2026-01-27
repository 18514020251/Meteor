package com.meteor.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.domain.enums.MerchantApplyStatusEnum;
import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.admin.service.IMerchantApplyService;
import com.meteor.common.domain.PageResult;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  商家申请服务实现类
 *
 * @author Programmer
 */
@Service
@RequiredArgsConstructor
public class MerchantApplyServiceImpl extends ServiceImpl<MerchantApplyMapper, MerchantApply>
        implements IMerchantApplyService {

    private final MerchantApplyMapper merchantApplyMapper;

    @Override
    public PageResult<MerchantApplyDTO> list(MerchantApplyQueryDTO query) {
        Page<MerchantApply> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<MerchantApply> wrapper = buildWrapper(query);

        merchantApplyMapper.selectPage(page, wrapper);

        List<MerchantApplyDTO> dtoList = page.getRecords().stream()
                .map(MerchantApplyDTO::fromEntity)
                .toList();

        return PageResult.of(dtoList, page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(Long id) {
        MerchantApply apply = getByIdOrThrow(id);

        if (!MerchantApplyStatusEnum.PENDING.getCode().equals(apply.getStatus())) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_ALREADY_REVIEWED);
        }

        apply.setStatus(MerchantApplyStatusEnum.APPROVED);
        apply.setRejectReason(null);
        apply.setReviewedBy(getReviewerId());
        apply.setReviewedTime(LocalDateTime.now());

        merchantApplyMapper.updateById(apply);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reject(Long id, String rejectReason) {
        MerchantApply apply = getByIdOrThrow(id);

        if (!MerchantApplyStatusEnum.PENDING.getCode().equals(apply.getStatus())) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_ALREADY_REVIEWED);
        }

        if (rejectReason == null || rejectReason.isBlank()) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_REJECT_REASON_REQUIRED);
        }

        apply.setStatus(MerchantApplyStatusEnum.REJECTED);
        apply.setRejectReason(rejectReason.trim());
        apply.setReviewedBy(getReviewerId());
        apply.setReviewedTime(LocalDateTime.now());

        merchantApplyMapper.updateById(apply);
    }

    private MerchantApply getByIdOrThrow(Long id) {
        MerchantApply apply = merchantApplyMapper.selectById(id);
        if (apply == null) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_NOT_EXIST);
        }
        return apply;
    }

    /**
     * 你后面会接 Sa-Token admin loginType，这里先留钩子。
     * 现在先用一个最安全的方式：没有登录信息就报错。
     */
    private Long getReviewerId() {
        return StpUtil.getLoginIdAsLong();
    }

    /*
     * 构建查询条件
     */
    private LambdaQueryWrapper<MerchantApply> buildWrapper(MerchantApplyQueryDTO query) {
        LambdaQueryWrapper<MerchantApply> wrapper = new LambdaQueryWrapper<>();
        if (query.getUserId() != null) {
            wrapper.eq(MerchantApply::getUserId, query.getUserId());
        }
        String shopName = query.getShopName();
        if (shopName != null && !shopName.isBlank()) {
            wrapper.like(MerchantApply::getShopName, shopName.trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(MerchantApply::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(MerchantApply::getCreateTime);
        return wrapper;
    }
}

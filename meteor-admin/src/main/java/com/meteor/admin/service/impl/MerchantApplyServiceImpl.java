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
@SuppressWarnings("squid:S1172")
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


    /**
     * <p>
     *      审批通过
     * </p>
     * @param id 商家申请 ID
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(Long id) {
        MerchantApply apply = getByIdOrThrow(id);

        apply.approve(getReviewerId(), LocalDateTime.now());

        merchantApplyMapper.updateById(apply);

        // 后续扩展点：审批通过后的副作用
        afterApproved(apply);
    }

    private void afterApproved(MerchantApply apply) {
        // 1) 清理 token/身份缓存（跨服务：需要发 MQ 给 user 或调用 user 服务）
        // 2) 发送消息提醒用户（跨服务：同样走 MQ）
    }

    /**
     * <p>
     *     拒绝策略
     * </p>
     * @param id 商家申请 ID
     * @param rejectReason 拒绝理由
     * */
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


    private Long getReviewerId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * <p>
     *     构建查询条件
     * </p>
     * */
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

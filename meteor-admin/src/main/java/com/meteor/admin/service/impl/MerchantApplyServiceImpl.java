package com.meteor.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.admin.service.IMerchantApplyReviewedService;
import com.meteor.admin.service.IMerchantApplyService;
import com.meteor.common.domain.PageResult;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.satoken.context.LoginContext;
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

    private final LoginContext loginContext;

    private final IMerchantApplyReviewedService merchantApplyReviewedService;

    @Override
    public PageResult<MerchantApplyDTO> list(MerchantApplyQueryDTO query) {
        Page<MerchantApply> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<MerchantApply> wrapper = buildWrapper(query);

        baseMapper.selectPage(page, wrapper);

        List<MerchantApplyDTO> dtoList = page.getRecords().stream()
                .map(MerchantApplyDTO::fromEntity)
                .toList();

        return PageResult.of(dtoList, page.getTotal(), query.getPageNum(), query.getPageSize());
    }


    /**
     * <p>
     *      审批通过
     * </p>
     * @param applyId 商家申请 ID
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByApplyId(Long applyId) {
        MerchantApply apply = getByApplyIdOrThrow(applyId);

        Long reviewerId = loginContext.currentLoginId();
        LocalDateTime now = LocalDateTime.now();
        apply.approve(reviewerId, now);

        int update = baseMapper.updateById(apply);
        if (update != 1) {
            throw new BizException(CommonErrorCode.DATA_ERROR);
        }

        merchantApplyReviewedService.send(apply);
        afterApproved(apply);
    }


    private void afterApproved(MerchantApply apply) {
        // NOTE: Async side effects (notifications / inbox messages) will be implemented later in message module.
    }

    /**
     * <p>拒绝策略</p>
     * @param applyId 商家申请 ID
     * @param rejectReason 拒绝理由
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void rejectByApplyId(Long applyId, String rejectReason) {
        MerchantApply apply = getByApplyIdOrThrow(applyId);

        Long reviewerId = loginContext.currentLoginId();
        LocalDateTime now = LocalDateTime.now();
        apply.reject(reviewerId, now, rejectReason);

        int update = baseMapper.updateById(apply);
        if (update != 1) {
            throw new BizException(CommonErrorCode.DATA_ERROR);
        }

        merchantApplyReviewedService.send(apply);
    }

    private MerchantApply getByApplyIdOrThrow(Long applyId) {
        MerchantApply apply = baseMapper.selectOne(new LambdaQueryWrapper<MerchantApply>().eq(MerchantApply::getApplyId, applyId));
        if (apply == null) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_NOT_EXIST);
        }
        return apply;
    }

    /**
     * <p>构建查询条件</p>
     *
     * @param query 查询条件
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

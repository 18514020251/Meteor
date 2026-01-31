package com.meteor.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.admin.service.IMerchantApplyNotifyService;
import com.meteor.admin.service.IMerchantApplyReviewedService;
import com.meteor.admin.service.IMerchantApplyService;
import com.meteor.common.domain.PageResult;
import com.meteor.satoken.context.LoginContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  商家申请服务实现类
 *  业务用例
 *
 * @author Programmer
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("squid:S1172")
public class MerchantApplyServiceImpl extends ServiceImpl<MerchantApplyMapper, MerchantApply>
        implements IMerchantApplyService {

    private final LoginContext loginContext;

    private final IMerchantApplyReviewedService merchantApplyReviewedService;

    private final MerchantApplyTxServiceImpl txService;

    private final IMerchantApplyNotifyService merchantApplyNotifyService;

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
    @Override
    public void approveByApplyId(Long applyId) {
        MerchantApply apply = txService.approvePersist(applyId);
        merchantApplyReviewedService.send(apply, () -> txService.markReviewedSent(apply.getId()));

        afterApproved(apply);
    }

    private void afterApproved(MerchantApply apply) {
        try {
            merchantApplyNotifyService.notifyApproved(apply);
        } catch (Exception e) {
            log.warn("notifyApproved failed, applyId={}, err={}", apply.getApplyId(), e, e);
        }
    }

    /**
     * <p>拒绝策略</p>
     * @param applyId 商家申请 ID
     * @param rejectReason 拒绝理由
     * */
    @Override
    public void rejectByApplyId(Long applyId, String rejectReason) {
        MerchantApply apply = txService.rejectPersist(applyId, rejectReason);
        merchantApplyReviewedService.send(apply, () -> txService.markReviewedSent(apply.getId()));

        afterRejected(apply);
    }

    private void afterRejected(MerchantApply apply) {
        try {
            merchantApplyNotifyService.notifyRejected(apply);
        } catch (Exception e) {
            log.warn("notifyRejected failed, applyId={}, err={}", apply.getApplyId(), e.toString(), e);
        }
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

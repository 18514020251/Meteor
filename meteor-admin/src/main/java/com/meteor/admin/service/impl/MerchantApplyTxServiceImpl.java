package com.meteor.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.common.enums.mq.MessageSendStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.satoken.context.LoginContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 商家申请事务服务实现类
 *
 * @author Programmer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantApplyTxServiceImpl {

    private final MerchantApplyMapper baseMapper;
    private final LoginContext loginContext;

    /**
     * 审核通过：事务内落库，返回更新后的记录
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantApply approvePersist(Long applyId) {
        MerchantApply apply = getByApplyIdOrThrow(applyId);
        return persistApproveUpdate(apply);
    }

    /**
     * 审核拒绝：事务内落库，返回更新后的记录
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantApply rejectPersist(Long applyId, String rejectReason) {
        MerchantApply apply = getByApplyIdOrThrow(applyId);
        return persistRejectUpdate(apply, rejectReason);
    }

    /**
     * 发送成功后标记
     */
    @Transactional(rollbackFor = Exception.class)
    public void markReviewedSent(Long adminRecordId) {
        int rows = baseMapper.update(
                null,
                new LambdaUpdateWrapper<MerchantApply>()
                        .eq(MerchantApply::getId, adminRecordId)
                        .eq(MerchantApply::getReviewedMsgSent, MessageSendStatusEnum.UNSENT.getCode())
                        .set(MerchantApply::getReviewedMsgSent, MessageSendStatusEnum.SENT.getCode())
                        .set(MerchantApply::getReviewedMsgSentTime, LocalDateTime.now())
        );

        if (rows == 0) {
            log.info("markReviewedSent skipped, maybe already SENT. id={}", adminRecordId);
        }
    }


    private MerchantApply persistApproveUpdate(MerchantApply apply) {
        Long reviewerId = loginContext.currentLoginId();
        LocalDateTime now = LocalDateTime.now();

        apply.approve(reviewerId, now);

        int update = baseMapper.updateById(apply);
        if (update != 1) {
            throw new BizException(CommonErrorCode.DATA_ERROR);
        }
        return apply;
    }

    private MerchantApply persistRejectUpdate(MerchantApply apply, String rejectReason) {
        Long reviewerId = loginContext.currentLoginId();
        LocalDateTime now = LocalDateTime.now();

        apply.reject(reviewerId, now, rejectReason);

        int update = baseMapper.updateById(apply);
        if (update != 1) {
            throw new BizException(CommonErrorCode.DATA_ERROR);
        }
        return apply;
    }

    private MerchantApply getByApplyIdOrThrow(Long applyId) {
        MerchantApply apply = baseMapper.selectOne(
                new LambdaQueryWrapper<MerchantApply>().eq(MerchantApply::getApplyId, applyId)
        );
        if (apply == null) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_NOT_EXIST);
        }
        return apply;
    }

}

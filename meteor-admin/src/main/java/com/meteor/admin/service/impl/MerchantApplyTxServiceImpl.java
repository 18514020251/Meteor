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
 *  商家申请事务服务实现类
 *
 * @author Programmer
 * @date 2026-01-28 23:41
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantApplyTxServiceImpl {

    private final MerchantApplyMapper baseMapper;
    private final LoginContext loginContext;

    @Transactional(rollbackFor = Exception.class)
    public MerchantApply approvePersist(Long applyId) {
        MerchantApply apply = baseMapper.selectOne(
                new LambdaQueryWrapper<MerchantApply>().eq(MerchantApply::getApplyId, applyId)
        );
        if (apply == null) {
            throw new BizException(CommonErrorCode.DATA_ERROR);
        }
        return persistReviewUpdate(apply);
    }

    public MerchantApply persistReviewUpdate(MerchantApply apply) {

        Long reviewerId = loginContext.currentLoginId();
        LocalDateTime now = LocalDateTime.now();

        apply.approve(reviewerId, now);

        int update = baseMapper.updateById(apply);
        if (update != 1) {
            throw new BizException(CommonErrorCode.DATA_ERROR);
        }

        return apply;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markReviewedSent(Long adminRecordId) {
        int rows = baseMapper.update(null,
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
}

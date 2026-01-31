package com.meteor.user.service.tx;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.domain.entity.User;
import com.meteor.user.enums.RoleEnum;
import com.meteor.user.mapper.MerchantApplyMapper;
import com.meteor.user.mapper.UserMapper;
import com.meteor.user.service.post.MerchantApprovedPostCommitActions;
import com.meteor.user.service.tx.model.TxResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商家申请审核 - 事务服务
 * 只做 DB：
 * 1) 更新 merchant_apply 状态
 * 2) APPROVED 时升级用户角色
 * @author Programmer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantApplyReviewedTxService {

    private final MerchantApplyMapper merchantApplyMapper;
    private final UserMapper userMapper;
    private final MerchantApprovedPostCommitActions postCommitActions;

    @Transactional(rollbackFor = Exception.class)
    public TxResult process(MerchantApplyReviewedMessage message) {

        MerchantApplyStatusEnum statusEnum =
                MerchantApplyStatusEnum.fromCode(message.getStatusCode());

        validate(message, statusEnum);

        int rows = merchantApplyMapper.update(null,
                new LambdaUpdateWrapper<MerchantApply>()
                        .eq(MerchantApply::getId, message.getApplyId())
                        .eq(MerchantApply::getStatus, MerchantApplyStatusEnum.PENDING.getCode())
                        .set(MerchantApply::getStatus, statusEnum.getCode())
        );

        if (rows <= 0) {
            return handleRowsZero(message, statusEnum);
        }

        if (statusEnum == MerchantApplyStatusEnum.APPROVED) {
            upgradeUserRoleToMerchant(message.getUserId());

            postCommitActions.onApproved(message);
        }

        return TxResult.updated(statusEnum);
    }

    /* ======================= private ======================= */

    private void validate(MerchantApplyReviewedMessage message,
                          MerchantApplyStatusEnum statusEnum) {
        if (message == null || statusEnum == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }
        if (statusEnum == MerchantApplyStatusEnum.APPROVED
                && message.getUserId() == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }
    }

    private TxResult handleRowsZero(MerchantApplyReviewedMessage message,
                                    MerchantApplyStatusEnum statusEnum) {

        MerchantApply apply = merchantApplyMapper.selectById(message.getApplyId());
        if (apply == null) {
            log.error("merchant_apply not found, applyId={}, msgStatus={}",
                    message.getApplyId(), statusEnum);
            return TxResult.notFound();
        }

        if (!MerchantApplyStatusEnum.PENDING.getCode().equals(apply.getStatus().getCode())) {
            log.info("merchant_apply already processed, applyId={}, dbStatus={}, msgStatus={}",
                    message.getApplyId(), apply.getStatus(), statusEnum);
            return TxResult.alreadyProcessed();
        }

        log.error("merchant_apply update failed but still PENDING, applyId={}", message.getApplyId());
        return TxResult.error();
    }

    private void upgradeUserRoleToMerchant(Long userId) {
        int updated = userMapper.update(null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getId, userId)
                        .ne(User::getRole, RoleEnum.MERCHANT.getCode())
                        .set(User::getRole, RoleEnum.MERCHANT.getCode())
        );

        if (updated == 0) {
            User u = userMapper.selectById(userId);
            if (u == null) {
                throw new BizException(CommonErrorCode.DATA_ERROR);
            }
            log.info("user already MERCHANT, userId={}", userId);
        }
    }
}

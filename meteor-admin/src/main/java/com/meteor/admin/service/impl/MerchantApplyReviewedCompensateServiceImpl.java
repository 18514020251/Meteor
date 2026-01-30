package com.meteor.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.domain.vo.ReviewedResendVO;
import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.admin.service.IMerchantApplyReviewedCompensateService;
import com.meteor.admin.service.IMerchantApplyReviewedService;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.common.enums.message.MessageSendStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *  商家申请审核结果补偿服务实现类
 *
 * @author Programmer
 * @date 2026-01-29 14:26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantApplyReviewedCompensateServiceImpl implements IMerchantApplyReviewedCompensateService {

    private final MerchantApplyMapper merchantApplyMapper;
    private final IMerchantApplyReviewedService reviewedSender;
    private final MerchantApplyTxServiceImpl txService;

    @Override
    public ReviewedResendVO resendByApplyId(Long applyId) {

        MerchantApply apply = merchantApplyMapper.selectOne(
                new LambdaQueryWrapper<MerchantApply>()
                        .eq(MerchantApply::getApplyId, applyId)
                        .last("limit 1")
        );

        if (apply == null) {
            return new ReviewedResendVO(false, false, "applyId 不存在");
        }

        if (apply.getStatus() == MerchantApplyStatusEnum.PENDING) {
            return new ReviewedResendVO(false, false, "该申请未审核，无法补发");
        }

        if (MessageSendStatusEnum.SENT.getCode().equals(apply.getReviewedMsgSent())) {
            return new ReviewedResendVO(true, true, "消息已发送，无需补发");
        }

        reviewedSender.send(apply, () -> txService.markReviewedSent(apply.getId()));

        MerchantApply refreshed = merchantApplyMapper.selectById(apply.getId());
        if (refreshed != null && MessageSendStatusEnum.SENT.getCode().equals(refreshed.getReviewedMsgSent())) {
            return new ReviewedResendVO(true, false, "补发成功");
        }

        return new ReviewedResendVO(false, false, "补发失败（MQ confirm/NO_ROUTE 未通过）");
    }
}

package com.meteor.user.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.common.mq.merchant.MerchantApplyReviewedMessage;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.mapper.MerchantApplyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *  商家申请审核结果消息队列消费者
 *
 * @author Programmer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantApplyReviewedConsumer {

    private final MerchantApplyMapper merchantApplyMapper;

    @RabbitListener(queues = MerchantApplyContract.Queue.MERCHANT_APPLY_REVIEWED)
    @Transactional(rollbackFor = Exception.class)
    public void handle(MerchantApplyReviewedMessage message) {

        if (message == null || message.getApplyId() == null || message.getStatus() == null) {
            throw new IllegalArgumentException("MerchantApplyReviewedMessage invalid: " + message);
        }

        MerchantApplyStatusEnum statusEnum = message.getStatus();

        LambdaUpdateWrapper<MerchantApply> updateWrapper =
                new LambdaUpdateWrapper<MerchantApply>()
                        .eq(MerchantApply::getId, message.getApplyId())
                        .eq(MerchantApply::getStatus, MerchantApplyStatusEnum.PENDING.getCode())
                        .set(MerchantApply::getStatus, statusEnum.getCode());

        int rows = merchantApplyMapper.update(null, updateWrapper);

        if (rows > 0) {
            log.info("MerchantApply reviewed success. applyId={}, status={}",
                    message.getApplyId(), statusEnum);
            return;
        }

        log.info("MerchantApply reviewed already processed. applyId={}", message.getApplyId());
    }
}

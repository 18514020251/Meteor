package com.meteor.admin.mq.consumer;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.common.mq.merchant.MerchantApplyCreatedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 *  商家申请消息队列消费者
 *
 * @author Programmer
 * @date 2026-01-23 11:41
 */
@Component
@RequiredArgsConstructor
public class MerchantApplyCreatedConsumer {

    private final MerchantApplyMapper mapper;

    @RabbitListener(queues = "merchant.apply.created.queue")
    public void handle(MerchantApplyCreatedMessage message) {

        if (mapper.existsByApplyId(message.getApplyId())) {
            return;
        }

        MerchantApply entity = new MerchantApply();
        entity.setApplyId(message.getApplyId());
        entity.setUserId(message.getUserId());
        entity.setShopName(message.getShopName());
        entity.setApplyReason(message.getApplyReason());
        entity.setStatus(0);
        entity.setCreateTime(message.getApplyTime());
        entity.setReviewedBy(null);
        entity.setReviewedTime(null);

        mapper.insert(entity);
    }
}

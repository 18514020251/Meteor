package com.meteor.admin.mq.consumer;

import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.admin.mq.assembler.MerchantApplyAssembler;
import com.meteor.common.mq.merchant.MerchantApplyCreatedMessage;
import com.meteor.common.mq.merchant.MerchantApplyEvent;
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
    private final MerchantApplyAssembler assembler;

    @RabbitListener(queues = MerchantApplyEvent.Queue.MERCHANT_APPLY_CREATED)
    public void handle(MerchantApplyCreatedMessage message) {

        if (mapper.existsByApplyId(message.getApplyId())) {
            return;
        }

        mapper.insert(assembler.from(message));
    }
}

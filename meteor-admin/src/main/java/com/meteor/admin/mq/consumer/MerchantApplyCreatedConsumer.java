package com.meteor.admin.mq.consumer;

import com.meteor.admin.mapper.MerchantApplyMapper;
import com.meteor.admin.mq.mapper.MerchantApplyMqAssembler;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.MerchantApplyCreatedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final MerchantApplyMqAssembler assembler;

    @RabbitListener(queues = MerchantApplyContract.Queue.MERCHANT_APPLY_CREATED  , errorHandler = "mqRejectErrorHandler")
    @Transactional(rollbackFor = Exception.class)
    public void handle(MerchantApplyCreatedMessage message) {

        if (mapper.existsByApplyId(message.getApplyId())) {
            return;
        }

        mapper.insert(assembler.from(message));
    }
}

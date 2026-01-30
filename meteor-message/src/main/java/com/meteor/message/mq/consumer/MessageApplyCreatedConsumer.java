package com.meteor.message.mq.consumer;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.message.domain.entity.UserMessage;
import com.meteor.message.mapper.UserMessageMapper;
import com.meteor.message.mq.assembler.MessageApplyMqAssembler;
import com.meteor.message.template.UserMessageTemplate;
import com.meteor.message.template.UserMessageTemplateRegistry;
import com.meteor.mq.contract.enums.message.UserEventType;
import com.meteor.mq.contract.message.UserEventMessage;
import com.meteor.mq.contract.message.UserMessageContract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *  消息申请消息队列消费者
 *
 * @author Programmer
 * @date 2026-01-30 17:04
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageApplyCreatedConsumer {

    private final UserMessageMapper mapper;
    private final UserMessageTemplateRegistry templateRegistry;
    private final MessageApplyMqAssembler assembler;

    @RabbitListener(queues = UserMessageContract.Queue.USER_MESSAGE_CREATED,
            errorHandler = "mqRejectErrorHandler")
    @Transactional(rollbackFor = Exception.class )
    public void handle(UserEventMessage msg) {
        validate(msg);

        UserEventType eventType = UserEventType.fromCode(msg.getEventType());
        if (eventType == null) {
            log.warn("未知 eventType: {}", msg.getEventType());
            return;
        }

        switch (eventType) {
            case USER_PASSWORD_CHANGED -> handlePasswordChanged(msg);
            case MERCHANT_APPLY_SUBMITTED -> handleMerchantApplySubmitted(msg);
            case MERCHANT_APPLY_REVIEWED -> handleMerchantApplyReviewed(msg);
            case MERCHANT_APPLY_REJECTED -> handleMerchantApplyRejected(msg);
            default -> log.warn("参数异常");
        }
    }

    private void handlePasswordChanged(UserEventMessage msg) {

        UserMessageTemplate tpl = templateRegistry.getByEventType(msg.getEventType());

        if (tpl == null) {
            log.warn("缺少模板 eventType={}, eventId={}", msg.getEventType(), msg.getEventId());
            return;
        }

        UserMessage entity = assembler.toEntity(msg, tpl);

        try {
            mapper.insert(entity);
        } catch (DuplicateKeyException e) {
            log.info("重复消费已忽略 bizKey={}, userId={}", entity.getBizKey(), entity.getUserId());
        }
    }

    private void handleMerchantApplyRejected(UserEventMessage msg) {
        // 方法待实现
    }

    private void handleMerchantApplyReviewed(UserEventMessage msg) {
        // 方法待实现
    }

    private void handleMerchantApplySubmitted(UserEventMessage msg) {
        // 方法待实现
    }

    private void validate(UserEventMessage msg) {
        if (msg == null
                || msg.getEventId() == null
                || msg.getEventType() == null
                || msg.getUserId() == null
                || msg.getOccurredAt() == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }
    }

}

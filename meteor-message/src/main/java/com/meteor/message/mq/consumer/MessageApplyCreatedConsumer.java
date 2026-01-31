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
    public void handle(UserEventMessage msg) {
        validate(msg);

        UserEventType eventType = UserEventType.fromCode(msg.getEventType());
        if (eventType == null) {
            throw new BizException(CommonErrorCode.INVALID_MQ_MESSAGE);
        }

        switch (eventType) {
            case USER_PASSWORD_CHANGED, MERCHANT_APPLY_SUBMITTED -> persistAsUserMessage(msg);
            case MERCHANT_APPLY_REVIEWED, MERCHANT_APPLY_REJECTED -> persistMessage(msg);
            default -> log.warn("未处理的 eventType={}, eventId={}", msg.getEventType(), msg.getEventId());
        }
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

    private void persistAsUserMessage(UserEventMessage msg) {
        persistMessageInternal(msg, (m, e) -> log.info("消息入库成功 type={}, bizKey={}, userId={}",
                e.getType(), e.getBizKey(), e.getUserId()));
    }

    private void persistMessage(UserEventMessage msg) {
        persistMessageInternal(msg, (m, e) -> log.info("消息入库成功 eventType={}, bizKey={}, userId={}",
                m.getEventType(), e.getBizKey(), e.getUserId()));
    }

    private void persistMessageInternal(UserEventMessage msg,
                                        java.util.function.BiConsumer<UserEventMessage, UserMessage> successLogger) {
        UserMessageTemplate tpl = templateRegistry.getByEventType(msg.getEventType());
        if (tpl == null) {
            log.warn("缺少模板 eventType={}, eventId={}", msg.getEventType(), msg.getEventId());
            return;
        }

        UserMessage entity = assembler.toEntity(msg, tpl);

        try {
            mapper.insert(entity);
            successLogger.accept(msg, entity);
        } catch (DuplicateKeyException e) {
            log.info("重复消费已忽略 bizKey={}, userId={}", entity.getBizKey(), entity.getUserId());
        }
    }
}

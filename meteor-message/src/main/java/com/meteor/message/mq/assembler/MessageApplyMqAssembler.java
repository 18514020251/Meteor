package com.meteor.message.mq.assembler;

import com.meteor.common.enums.DeleteStatus;
import com.meteor.common.enums.message.MessageReadStatusEnum;
import com.meteor.common.enums.message.MessageSourceEnum;
import com.meteor.message.constants.MessageConstants;
import com.meteor.message.domain.entity.UserMessage;
import com.meteor.message.template.UserMessageTemplate;
import com.meteor.message.template.UserMessageTemplateRenderer;
import com.meteor.mq.contract.message.UserEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *  消息 MQ消息构造器
 *
 * @author Programmer
 * @date 2026-01-30 18:14
 */
@Component
@RequiredArgsConstructor
public class MessageApplyMqAssembler {

    private final UserMessageTemplateRenderer renderer;

    public UserMessage toEntity(UserEventMessage msg, UserMessageTemplate tpl) {
        UserMessage entity = new UserMessage();
        entity.setUserId(msg.getUserId());
        entity.setSource(MessageSourceEnum.EVENT);

        entity.setType(msg.getEventType());
        entity.setTitle(tpl.getTitleTemplate());
        entity.setContent(renderer.renderContent(
                tpl.getContentTemplate(),
                msg.getOccurredAt(),
                msg.getPayload()
        ));

        entity.setBizKey(MessageConstants.BIZ_KEY_EVENT_PREFIX + msg.getEventId());

        entity.setReadStatus(MessageReadStatusEnum.UNREAD);
        entity.setDeleted(DeleteStatus.NORMAL);

        entity.setCreateTime(msg.getOccurredAt());

        return entity;
    }
}

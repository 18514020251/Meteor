package com.meteor.message.service.assembler;

import com.meteor.message.domain.entity.UserMessage;
import com.meteor.message.controller.vo.UserMessageVO;

/**
 *  用户消息 Assembler
 *
 * @author Programmer
 * @date 2026-01-29 18:31
 */
public class UserMessageAssembler {
    private UserMessageAssembler(){}
    public static UserMessageVO toVO(UserMessage e) {
        if (e == null) {
            return null;
        }
        return new UserMessageVO(
                e.getId(),
                e.getType(),
                e.getTitle(),
                e.getContent(),
                e.getReadStatus().getCode(),
                e.getCreateTime(),
                e.getReadTime()
        );
    }
}

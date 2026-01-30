package com.meteor.message.domain.assembler;

import com.meteor.message.domain.entity.UserMessage;
import com.meteor.message.domain.vo.UserMessageVO;

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
        UserMessageVO vo = new UserMessageVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        vo.setTitle(e.getTitle());
        vo.setContent(e.getContent());
        vo.setReadStatus(e.getReadStatus().getCode());
        vo.setCreateTime(e.getCreateTime());
        vo.setReadTime(e.getReadTime());
        return vo;
    }
}

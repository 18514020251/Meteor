package com.meteor.message.template;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  用户消息模板
 *
 * @author Programmer
 * @date 2026-01-30 17:00
 */
@Getter
@AllArgsConstructor
public class UserMessageTemplate {

    private final String titleTemplate;
    private final String contentTemplate;
}

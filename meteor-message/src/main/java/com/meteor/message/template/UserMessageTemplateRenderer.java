package com.meteor.message.template;

import com.meteor.message.constants.MessageConstants;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Programmer
 * @date 2026-01-30 17:02
 */
@Component
public class UserMessageTemplateRenderer {

    private static final DateTimeFormatter DF =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String renderContent(String template, LocalDateTime occurredAt) {
        if (template == null) {
            return null;
        }
        if (occurredAt == null) {
            return template;
        }
        return template.replace(MessageConstants.PLACEHOLDER_OCCURRED_AT, DF.format(occurredAt));
    }
}

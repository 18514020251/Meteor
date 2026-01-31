package com.meteor.message.domain.template;

import com.meteor.message.constants.MessageConstants;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 *  用户消息模板渲染器
 *
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

    public String renderContent(String template, LocalDateTime occurredAt, Map<String, String> payload) {
        String content = renderContent(template, occurredAt);
        if (content == null || payload == null || payload.isEmpty()) {
            return content;
        }
        String shopName = payload.get("shopName");
        if (shopName != null) {
            content = content.replace("{shopName}", shopName);
        }
        String reason = payload.get("reason");
        if (reason != null) {
            content = content.replace("{reason}", reason);
        }

        return content;
    }
}

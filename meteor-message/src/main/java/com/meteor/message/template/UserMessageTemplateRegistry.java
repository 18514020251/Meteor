package com.meteor.message.template;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Programmer
 * @date 2026-01-30 17:01
 */
@Component
public class UserMessageTemplateRegistry {

    private final Map<Integer, UserMessageTemplate> templateMap = new HashMap<>();

    @PostConstruct
    public void init() {
        templateMap.put(0, new UserMessageTemplate(
                "密码已修改",
                "你的账号密码已于 {occurredAt} 修改。如非本人操作，请尽快找回密码或联系客服。"
        ));
    }

    public UserMessageTemplate getByEventType(Integer eventType) {
        return templateMap.get(eventType);
    }
}

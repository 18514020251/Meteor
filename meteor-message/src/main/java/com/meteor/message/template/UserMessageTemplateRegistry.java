package com.meteor.message.template;

import com.meteor.mq.contract.enums.message.UserEventType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知消息模板
 *
 * @author Programmer
 * @date 2026-01-30 17:01
 */
@Component
public class UserMessageTemplateRegistry {

    private final Map<Integer, UserMessageTemplate> templateMap = new HashMap<>();

    @PostConstruct
    public void init() {
        templateMap.put(UserEventType.USER_PASSWORD_CHANGED.getCode(), new UserMessageTemplate(
                "密码已修改",
                "你的账号密码已于 {occurredAt} 修改。如非本人操作，请尽快找回密码或联系客服。"
        ));

        templateMap.put(UserEventType.MERCHANT_APPLY_SUBMITTED.getCode(), new UserMessageTemplate(
                "商家申请已提交",
                "你于 {occurredAt} 提交了商家申请（店铺：{shopName}），请等待管理员审核。"
        ));

        templateMap.put(UserEventType.MERCHANT_APPLY_REVIEWED.getCode(), new UserMessageTemplate(
                "商家申请已通过",
                "你提交的商家申请（店铺：{shopName}）已于 {occurredAt} 审核通过，现在可以正常使用商家功能。"
        ));

        templateMap.put(UserEventType.MERCHANT_APPLY_REJECTED.getCode(), new UserMessageTemplate(
                "商家申请未通过",
                "你提交的商家申请（店铺：{shopName}）已于 {occurredAt} 被拒绝，原因：{reason}。你可以修改后重新提交。"
        ));

    }

    public UserMessageTemplate getByEventType(Integer eventType) {
        return templateMap.get(eventType);
    }
}

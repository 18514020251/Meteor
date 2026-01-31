package com.meteor.admin.service.assembler;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.enums.message.UserEventType;
import com.meteor.mq.contract.message.UserEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *  商家申请结果 assembler
 *
 * @author Programmer
 * @date 2026-01-30 21:50
 */
@Component
@RequiredArgsConstructor
public class MerchantApplyNotifyMqAssembler {

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public UserEventMessage toApprovedNotifyMessage(MerchantApply apply) {
        return build(apply,
                UserEventType.MERCHANT_APPLY_REVIEWED,
                basePayload(apply)
        );
    }

    public UserEventMessage toRejectedNotifyMessage(MerchantApply apply) {
        Map<String, String> payload = new java.util.HashMap<>(basePayload(apply));
        payload.put("reason", nvl(apply.getRejectReason()));
        return build(apply,
                UserEventType.MERCHANT_APPLY_REJECTED,
                payload
        );
    }

    private UserEventMessage build(MerchantApply apply,
                                   UserEventType eventType,
                                   Map<String, String> payload) {
        UserEventMessage msg = new UserEventMessage();
        msg.setEventId(snowflakeIdGenerator.nextId());
        msg.setEventType(eventType.getCode());
        msg.setUserId(apply.getUserId());
        msg.setBizId(applyIdStr(apply));
        msg.setOccurredAt(occurredAt(apply));
        msg.setPayload(payload);
        return msg;
    }

    private Map<String, String> basePayload(MerchantApply apply) {
        Map<String, String> payload = new java.util.HashMap<>();
        payload.put("applyId", applyIdStr(apply));
        payload.put("shopName", nvl(apply.getShopName()));
        return payload;
    }

    private String applyIdStr(MerchantApply apply) {
        return String.valueOf(apply.getApplyId());
    }

    private java.time.LocalDateTime occurredAt(MerchantApply apply) {
        return apply.getReviewedTime() != null
                ? apply.getReviewedTime()
                : java.time.LocalDateTime.now();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}


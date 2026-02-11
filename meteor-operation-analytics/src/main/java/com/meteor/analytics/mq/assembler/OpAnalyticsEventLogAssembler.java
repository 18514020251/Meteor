package com.meteor.analytics.mq.assembler;

import com.meteor.analytics.domain.entity.OpAnalyticsEventLog;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 21:12
 */
@Component
public class OpAnalyticsEventLogAssembler {

    public OpAnalyticsEventLog build(String eventType, String eventKey) {
        return build(eventType, eventKey, LocalDateTime.now());
    }

    public OpAnalyticsEventLog build(String eventType, String eventKey, LocalDateTime createTime) {
        OpAnalyticsEventLog e = new OpAnalyticsEventLog();
        e.setEventType(eventType);
        e.setEventKey(eventKey);
        e.setCreateTime(createTime);
        return e;
    }
}

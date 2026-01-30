package com.meteor.mq.contract.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 *  用户消息创建消息
 *
 * @author Programmer
 * @date 2026-01-29 21:17
 */
@Data
public class UserEventMessage implements Serializable {

    // 幂等ID
    private Long eventId;

    // 事件类型
    private Integer eventType;

    // 目标用户
    private Long userId;

    // 关联业务ID（可空）
    private String bizId;

    // 事件发生时间
    private LocalDateTime occurredAt;

    // 事件附加参数
    private Map<String, String> payload;
}
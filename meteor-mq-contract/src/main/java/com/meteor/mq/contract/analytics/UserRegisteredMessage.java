package com.meteor.mq.contract.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *  用户注册成功消息
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 10:28
 */
@Data
@AllArgsConstructor
public class UserRegisteredMessage implements Serializable {

    /**
     * 事件唯一ID（幂等去重使用）
     * 建议格式：ur:{snowflakeId}
     */
    private String eventId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 注册成功时间
     */
    private LocalDateTime occurTime;
}

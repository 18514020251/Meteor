package com.meteor.ticketing.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 *  抢票下单消息消费日志
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 16:49
 */
@Data
public class TicketMqConsumeLog {

    private Long id;

    /** 消息唯一键（orderNo） */
    private String msgKey;

    /** 消息主题 */
    private String topic;

    private LocalDateTime createTime;
}

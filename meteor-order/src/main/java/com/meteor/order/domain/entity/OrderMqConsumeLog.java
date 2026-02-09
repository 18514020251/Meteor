package com.meteor.order.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *  MQ 消费去重表
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 17:11
 */
@Data
@TableName("order_mq_consume_log")
public class OrderMqConsumeLog {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一键（orderNo / eventId）
     */
    private String msgKey;

    /**
     * 消息主题
     */
    private String topic;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

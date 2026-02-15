package com.meteor.ticketing.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.meteor.ticketing.enums.OutboxStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *  MQ 出站事件实体类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mq_outbox_event")
@Schema(description="MqOutboxEvent对象")
public class MqOutboxEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("biz_key")
    private String bizKey;

    @TableField("event_type")
    private String eventType;

    @TableField("exchange_name")
    private String exchangeName;

    @TableField("routing_key")
    private String routingKey;

    @TableField("payload")
    private String payload;

    @TableField("status")
    private OutboxStatus status;

    @TableField("retry_cnt")
    private Integer retryCnt;

    @TableField("next_retry_time")
    private LocalDateTime nextRetryTime;

    @TableField("deliver_at")
    private LocalDateTime deliverAt;

    @TableField("biz_expire_at")
    private LocalDateTime bizExpireAt;

    @TableField("trace_id")
    private String traceId;

    @TableField("last_error")
    private String lastError;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

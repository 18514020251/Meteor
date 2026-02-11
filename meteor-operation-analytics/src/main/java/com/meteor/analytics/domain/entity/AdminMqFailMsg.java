package com.meteor.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 管理端-MQ失败/待补发表
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("admin_mq_fail_msg")
@Schema(description="管理端-MQ失败/待补发表")
public class AdminMqFailMsg implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "消息唯一ID")
    private String msgId;

    private String exchangeName;

    private String routingKey;

    @Schema(description = "可选：业务topic")
    private String topic;

    @Schema(description = "展示名")
    private String name;

    @Schema(description = "warn/error")
    private String level;

    @Schema(description = "PENDING/FAILED/DONE")
    private String status;

    private Integer retryCnt;

    private String lastError;

    private LocalDateTime nextRetryTime;

    @Schema(description = "原始消息体(便于补发)")
    private String payload;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}

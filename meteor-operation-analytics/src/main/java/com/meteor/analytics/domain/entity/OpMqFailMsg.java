package com.meteor.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.meteor.analytics.enums.SendState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 运营分析-失败消息中心表
 * </p>
 *
 * @author Programmer
 * @since 2026-02-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("op_mq_fail_msg")
@Schema(description="运营分析-失败消息中心表")
public class OpMqFailMsg implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "来源服务：USER/MERCHANT/ADMIN")
    @TableField("source_module")
    private String sourceModule;

    @Schema(description = "源服务内消息唯一ID")
    @TableField("msg_id")
    private String msgId;

    @TableField("biz_id")
    private Long bizId;

    @TableField("exchange_name")
    private String exchangeName;

    @TableField("routing_key")
    private String routingKey;

    @TableField("topic")
    private String topic;

    @TableField("payload")
    private String payload;

    @Schema(description = "0=PENDING,1=DONE,2=FAILED")
    @TableField("status")
    private Integer status;

    @TableField("retry_cnt")
    private Integer retryCnt;

    @TableField("next_retry_time")
    private LocalDateTime nextRetryTime;

    @TableField("last_error")
    private String lastError;

    @TableField("source_create_time")
    private LocalDateTime sourceCreateTime;

    @TableField("source_update_time")
    private LocalDateTime sourceUpdateTime;

    @TableField("collect_time")
    private LocalDateTime collectTime;

    @TableField("collect_version")
    private Long collectVersion;

    @TableField("resend_state")
    private SendState resendState;

    @TableField("resend_request_id")
    private String resendRequestId;

    @TableField("resend_attempt_cnt")
    private Integer resendAttemptCnt;

    @TableField("resend_last_time")
    private LocalDateTime resendLastTime;

    @TableField("resend_last_error")
    private String resendLastError;

    @TableField("row_version")
    private Long rowVersion;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}


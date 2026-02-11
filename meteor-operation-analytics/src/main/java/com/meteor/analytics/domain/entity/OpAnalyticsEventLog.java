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
 * 运营统计事件去重表
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("op_analytics_event_log")
@Schema(description="运营统计事件去重表")
public class OpAnalyticsEventLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "事件唯一键")
    private String eventKey;

    @Schema(description = "USER_REGISTERED/PAY_CREATED/PAY_SUCCESS")
    private String eventType;

    private LocalDateTime createTime;


}

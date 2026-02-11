package com.meteor.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.meteor.analytics.enums.BizScopeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 运营分析-每日KPI汇总
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("op_analytics_daily")
@Schema(description="运营分析-每日KPI汇总")
public class OpAnalyticsDaily implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "统计日期(自然日)")
    private LocalDate statDate;

    @Schema(description = "统计范围: GLOBAL/MERCHANT")
    private BizScopeEnum bizScope;

    @Schema(description = "范围ID: 0=全局; merchant_id等")
    private Long bizId;

    @Schema(description = "当日注册数")
    private Integer registerCnt;

    @Schema(description = "当日支付尝试次数(支付单创建数)")
    private Integer payAttemptCnt;

    @Schema(description = "当日支付成功次数(支付成功数)")
    private Integer paySuccessCnt;

    @Schema(description = "当日成交订单数(以支付成功去重order_no)")
    private Integer dealOrderCnt;

    @Schema(description = "当日成交额(分)")
    private Long gmvCent;

    @Schema(description = "成功率(基点, 10000=100%)")
    private Integer successRateBp;

    @Schema(description = "口径版本")
    private Integer calcVersion;

    @Schema(description = "计算/刷新时间")
    private LocalDateTime calcTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}

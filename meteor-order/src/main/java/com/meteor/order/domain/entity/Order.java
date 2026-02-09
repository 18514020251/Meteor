package com.meteor.order.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 订单主表
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order")
@Schema(description="Order对象")
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID（雪花算法生成）")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单号(业务唯一)")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商家ID(影院方/售票方)")
    private Long merchantId;

    @Schema(description = "0=WAIT_PAY 1=PAID 2=CANCELED 3=CLOSED_TIMEOUT 4=REFUNDING 5=REFUNDED")
    private Integer status;

    @Schema(description = "业务类型 1=电影票")
    private Integer bizType;

    @Schema(description = "总金额(分)")
    private Integer totalAmount;

    @Schema(description = "实付金额(分)")
    private Integer payAmount;

    @Schema(description = "优惠金额(分)")
    private Integer discountAmount;

    @Schema(description = "支付截止时间")
    private LocalDateTime expireTime;

    @Schema(description = "支付成功时间")
    private LocalDateTime payTime;

    @Schema(description = "关闭时间(取消/超时)")
    private LocalDateTime closeTime;

    @Schema(description = "取消原因(用户取消/超时等)")
    private String cancelReason;

    @Schema(description = "0=NONE 1=ALIPAY 2=WECHAT")
    private Integer payChannel;

    @Schema(description = "支付单号(本系统生成，可为空待创建)")
    private String payNo;

    @Schema(description = "幂等键(防重复下单)")
    private String idempotentKey;

    @Schema(description = "请求追踪ID(可选)")
    private String requestId;

    @Schema(description = "扩展字段")
    private String extra;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "是否删除 0=否 1=是")
    private Integer deleted;


}

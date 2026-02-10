package com.meteor.order.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.order.enums.PayChannelEnum;
import com.meteor.order.enums.PaymentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 支付记录表
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("payment")
@Schema(description="Payment对象")
public class Payment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "支付记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "支付单号(系统生成唯一)")
    private String payNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "1=ALIPAY 2=WECHAT")
    private PayChannelEnum channel;

    @Schema(description = "0=INIT 1=SUCCESS 2=FAIL 3=CLOSED")
    private PaymentStatusEnum status;

    @Schema(description = "支付金额(分)")
    private Integer amount;

    @Schema(description = "第三方交易号")
    private String thirdTradeNo;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "是否删除 0=否 1=是")
    private DeleteStatus deleted;


}

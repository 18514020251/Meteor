package com.meteor.order.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.meteor.order.enums.OperatorTypeEnum;
import com.meteor.order.enums.OrderOperateTypeEnum;
import com.meteor.order.enums.OrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单操作日志
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_operate_log")
@Schema(description="OrderOperateLog对象")
public class OrderOperateLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "变更前状态")
    private Integer fromStatus;

    @Schema(description = "变更后状态")
    private OrderStatusEnum toStatus;

    @Schema(description = "1=CREATE 2=PAY_SUCCESS 3=CANCEL 4=CLOSE_TIMEOUT 5=REFUND")
    private OrderOperateTypeEnum operateType;

    @Schema(description = "1=USER 2=SYSTEM 3=ADMIN")
    private OperatorTypeEnum operatorType;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "备注")
    private String remark;

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

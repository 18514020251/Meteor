package com.meteor.order.domain.entity;

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
 * 订单明细表(按张数)
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_item")
@Schema(description="OrderItem对象")
public class OrderItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "明细ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "场次ID(screening.id)")
    private Long screeningId;

    @Schema(description = "电影ID(movie.id)")
    private Long movieId;

    @Schema(description = "商家ID")
    private Long merchantId;

    @Schema(description = "购票张数")
    private Integer ticketCount;

    @Schema(description = "单价(分)")
    private Integer unitPrice;

    @Schema(description = "小计(分)")
    private Integer amount;

    @Schema(description = "快照(片名/海报/开场时间/售卖方式等)")
    private String snapshot;

    @Schema(description = "扩展字段(未来座位/服务费等)")
    private String extra;

    @Schema(description = "0=WAIT_PAY 1=PAID 2=CANCELED 3=CLOSED_TIMEOUT 4=REFUNDING 5=REFUNDED")
    private Integer status;

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

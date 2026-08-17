package com.meteor.ticketing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.meteor.ticketing.enums.ReservationStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 票务库存预留表
 * </p>
 *
 * @author 昭兮
 * @since 2026-08-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ticket_inventory_reservation")
public class TicketInventoryReservation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 库存预留业务ID，当前直接复用 requestId
     */
    @TableId(value = "reservation_id", type = IdType.NONE)
    private String reservationId;

    /**
     * 客户端请求幂等ID
     */
    private String clientRequestId;

    /**
     * 场次ID
     */
    private Long screeningId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 预留票数
     */
    private Integer quantity;

    /**
     * PRE_RESERVED / CONFIRMED / RELEASED / COMPENSATED
     */
    private ReservationStatus status;

    /**
     * 业务过期时间，当前阶段暂不赋值
     */
    private LocalDateTime expireAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


}

package com.meteor.order.service;

import com.meteor.order.domain.entity.OrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * <p>
 * 订单明细表(按张数) 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
public interface IOrderItemService extends IService<OrderItem> {

    OrderItem getOneByOrderNo(String orderNo);

    boolean closeTimeoutItems(String orderNo, Long operatorId, LocalDateTime now);

}

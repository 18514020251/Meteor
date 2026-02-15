package com.meteor.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meteor.mq.contract.ticketing.TicketOrderDbReservedMessage;
import com.meteor.order.controller.vo.OrderDetailVO;
import com.meteor.order.controller.vo.pay.OrderListItemVO;
import com.meteor.order.domain.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.order.enums.OrderStatusEnum;

/**
 * <p>
 * 订单主表 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
public interface IOrderService extends IService<Order> {

    OrderDetailVO detail(String orderNo, Long userId);

    void createOrderFromTicket(TicketOrderDbReservedMessage message);

    Page<OrderListItemVO> page(Long userId, int page, int size, OrderStatusEnum status);

    void delete(String orderNo, Long uid);
}

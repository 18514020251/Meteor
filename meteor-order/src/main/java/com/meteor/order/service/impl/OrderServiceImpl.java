package com.meteor.order.service.impl;

import com.meteor.order.domain.entity.Order;
import com.meteor.order.mapper.OrderMapper;
import com.meteor.order.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单主表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

}

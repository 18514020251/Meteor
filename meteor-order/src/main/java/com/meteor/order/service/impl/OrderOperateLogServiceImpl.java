package com.meteor.order.service.impl;

import com.meteor.order.domain.entity.OrderOperateLog;
import com.meteor.order.mapper.OrderOperateLogMapper;
import com.meteor.order.service.IOrderOperateLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单操作日志 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Service
public class OrderOperateLogServiceImpl extends ServiceImpl<OrderOperateLogMapper, OrderOperateLog> implements IOrderOperateLogService {

}

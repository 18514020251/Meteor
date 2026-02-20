package com.meteor.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.order.controller.vo.OrderOperateLogExportVO;
import com.meteor.order.domain.entity.OrderOperateLog;
import com.meteor.order.enums.OperatorTypeEnum;
import com.meteor.order.enums.OrderOperateTypeEnum;
import com.meteor.order.enums.OrderStatusEnum;
import com.meteor.order.mapper.OrderOperateLogMapper;
import com.meteor.order.service.IOrderOperateLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单操作日志 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Service
@Slf4j
public class OrderOperateLogServiceImpl extends ServiceImpl<OrderOperateLogMapper, OrderOperateLog> implements IOrderOperateLogService {

    @Override
    public List<OrderOperateLogExportVO> listExportRowsByDate(LocalDate date) {
        Assert.notNull(date, "date must not be null");
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<OrderOperateLog> list = this.list(new LambdaQueryWrapper<OrderOperateLog>()
                .ge(OrderOperateLog::getCreateTime, start)
                .lt(OrderOperateLog::getCreateTime, end)
                .eq(OrderOperateLog::getDeleted, DeleteStatus.NORMAL)
                .orderByAsc(OrderOperateLog::getId));

        log.info("[exportByDate] date={}, rows={}", date, list.size());
        return list.stream().map(this::toExportVO).toList();
    }

    private OrderOperateLogExportVO toExportVO(OrderOperateLog e) {
        return new OrderOperateLogExportVO(
                e.getId(),
                e.getOrderNo(),
                e.getOrderId(),
                enumText(e.getOperateType()),
                enumText(e.getOperatorType()),
                e.getOperatorId(),
                enumText(e.getFromStatus()),
                enumText(e.getToStatus()),
                e.getRemark(),
                e.getCreateTime()
        );
    }

    private String enumText(OrderOperateTypeEnum e) {
        return e == null ? "" : e.getDesc();
    }
    private String enumText(OperatorTypeEnum e) {
        return e == null ? "" : e.name();
    }
    private String enumText(OrderStatusEnum e) {
        return e == null ? "" : e.getDesc();
    }

    @Override
    public String buildExportFileName(LocalDate date) {
        return "订单操作日志_" + date + ".xlsx";
    }
}

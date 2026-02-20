package com.meteor.order.service;

import com.meteor.order.controller.vo.OrderOperateLogExportVO;
import com.meteor.order.domain.entity.OrderOperateLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 订单操作日志 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
public interface IOrderOperateLogService extends IService<OrderOperateLog> {

    /**
     * 按天查询订单操作日志并转为导出行
     */
    List<OrderOperateLogExportVO> listExportRowsByDate(LocalDate date);

    /**
     * 生成导出文件名
     */
    String buildExportFileName(LocalDate date);
}

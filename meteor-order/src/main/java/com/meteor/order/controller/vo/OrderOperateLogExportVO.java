package com.meteor.order.controller.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *  订单操作日志导出VO
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-20 11:29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderOperateLogExportVO {

    @ExcelProperty("日志ID")
    private Long id;

    @ExcelProperty("订单号")
    private String orderNo;

    @ExcelProperty("订单ID")
    private Long orderId;

    @ExcelProperty("操作类型")
    private String operateType;

    @ExcelProperty("操作人类型")
    private String operatorType;

    @ExcelProperty("操作人ID")
    private Long operatorId;

    @ExcelProperty("变更前状态")
    private String fromStatus;

    @ExcelProperty("变更后状态")
    private String toStatus;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    @com.alibaba.excel.annotation.format.DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}


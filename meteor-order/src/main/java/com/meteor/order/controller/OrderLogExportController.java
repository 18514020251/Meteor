package com.meteor.order.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.meteor.order.controller.vo.OrderOperateLogExportVO;
import com.meteor.order.service.IOrderOperateLogService;
import com.meteor.satoken.constants.RoleConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@Tag(name = "导出-订单日志")
public class OrderLogExportController {

    private final IOrderOperateLogService orderOperateLogService;

    @GetMapping("/order-logs/export")
    @SaCheckRole(RoleConst.ADMIN)
    @Operation(summary = "按天导出订单操作日志")
    public void exportByDate(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            HttpServletResponse resp
    ) throws IOException {

        if (date == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "参数 date 必填，格式 YYYY-MM-DD");
            return;
        }

        List<OrderOperateLogExportVO> rows = orderOperateLogService.listExportRowsByDate(date);
        String fileName = orderOperateLogService.buildExportFileName(date);
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setCharacterEncoding("utf-8");
        resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        try (ServletOutputStream os = resp.getOutputStream()) {
            EasyExcelFactory
                    .write(os, OrderOperateLogExportVO.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("日志")
                    .doWrite(rows);
            os.flush();
        }
    }
}

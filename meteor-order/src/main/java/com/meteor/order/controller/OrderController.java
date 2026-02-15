package com.meteor.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meteor.common.result.Result;
import com.meteor.order.controller.vo.OrderDetailVO;
import com.meteor.order.controller.vo.pay.OrderListItemVO;
import com.meteor.order.enums.OrderStatusEnum;
import com.meteor.order.service.IOrderService;
import com.meteor.satoken.context.LoginContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单主表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final LoginContext loginContext;
    private final IOrderService orderService;

    @Operation(summary = "订单详情")
    @GetMapping()
    public Result<OrderDetailVO> detail(@RequestParam @NotBlank String orderNo) {
        Long uid = loginContext.currentLoginId();
        return Result.success(orderService.detail(orderNo, uid));
    }

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public Result<Page<OrderListItemVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatusEnum status
    ) {
        Long uid = loginContext.currentLoginId();
        return Result.success(orderService.page(uid, page, size, status));
    }

    @Operation(summary = "删除订单(仅关闭/取消允许)")
    @DeleteMapping()
    public Result<Void> delete(@RequestParam String orderNo) {
        Long uid = loginContext.currentLoginId();
        orderService.delete(orderNo, uid);
        return Result.success(null);
    }

}

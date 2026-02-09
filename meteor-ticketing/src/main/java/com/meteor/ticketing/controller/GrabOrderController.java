package com.meteor.ticketing.controller;

import com.meteor.common.result.Result;
import com.meteor.satoken.context.LoginContext;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.service.IGrabOrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 抢票下单入口（最小闭环：Lua扣库存 + 发MQ）
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 11:42
 */

@RestController
@RequestMapping("/ticketing/order")
@RequiredArgsConstructor
@Validated
public class GrabOrderController {

    private final IGrabOrderService grabOrderService;

    private final LoginContext loginContext;

    @Operation(summary = "抢票下单")
    @PostMapping("/grab")
    public Result<GrabOrderVO> grab(@RequestParam @NotNull Long screeningId) {
        Long uid = loginContext.currentLoginId();
        return Result.success(grabOrderService.grab(screeningId,uid));
    }
}

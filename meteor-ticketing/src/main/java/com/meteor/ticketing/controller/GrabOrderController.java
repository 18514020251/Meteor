package com.meteor.ticketing.controller;

import com.meteor.common.result.Result;
import com.meteor.satoken.context.LoginContext;
import com.meteor.ticketing.controller.dto.GrabOrderDTO;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.service.IGrabOrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.opentelemetry.api.trace.Span;

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
    public Result<GrabOrderVO> grab(@RequestBody @Valid GrabOrderDTO dto) {
        Long uid = loginContext.currentLoginId();
        Long screeningId = dto.getScreeningId();
        String clientRequestId = dto.getClientRequestId();

        Span span = Span.current();
        span.setAttribute("biz.screening_id", String.valueOf(screeningId));
        span.setAttribute("biz.user_id", String.valueOf(uid));

        try {
            GrabOrderVO vo = grabOrderService.grab(screeningId, uid, clientRequestId);
            span.setAttribute("biz.grab_result", "SUCCESS");
            return Result.success(vo);
        } catch (Exception e) {
            span.setAttribute("biz.grab_result", "FAILED");
            span.recordException(e);
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR);
            throw e;
        }
    }
}

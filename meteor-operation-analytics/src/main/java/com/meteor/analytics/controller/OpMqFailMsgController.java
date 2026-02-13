package com.meteor.analytics.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.meteor.analytics.controller.vo.ResendAllVO;
import com.meteor.analytics.controller.vo.ResendOneVO;
import com.meteor.analytics.service.IOpMqFailMsgService;
import com.meteor.common.result.Result;
import com.meteor.satoken.constants.RoleConst;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 运营分析-失败消息中心表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-02-13
 */
@SaCheckRole(RoleConst.ADMIN)
@RestController
@RequestMapping("/op-analytics/mqfail")
@RequiredArgsConstructor
public class OpMqFailMsgController {

    private final IOpMqFailMsgService resendService;

    /**
     * 一键全量补发：把所有未 SUCCESS 的记录都重试一遍
     */
    @Operation(summary = "一键全量补发（无条件）")
    @PostMapping("/resend/all")
    public Result<ResendAllVO> resendAll() {
        IOpMqFailMsgService.ResendSummary s = resendService.resendAll();
        return Result.success(ResendAllVO.from(s));
    }

    @Operation(summary = "补发单条（按ID）")
    @PostMapping("/resend/{id}")
    public Result<ResendOneVO> resendOne(@PathVariable("id") Long id) {
        IOpMqFailMsgService.ResendOneResult r = resendService.resendOne(id);
        return Result.success(ResendOneVO.from(r));
    }

    /**
     * 获取所有待补发/未成功的 MQ 消息
     * - resend_state in (WAIT, FAILED) 或 DOING 超时
     * GET /mq/fail-msg/pending?doingTimeoutMinutes=5&limit=500
     */
    @GetMapping("/fail-msg/pending")
    public Result<List<IOpMqFailMsgService.PendingMqMsgDTO>> listPending(
            @RequestParam(required = false) Integer doingTimeoutMinutes,
            @RequestParam(required = false) Integer limit
    ) {
        return Result.success(resendService.listPendingMqMsgs(doingTimeoutMinutes, limit));
    }





}

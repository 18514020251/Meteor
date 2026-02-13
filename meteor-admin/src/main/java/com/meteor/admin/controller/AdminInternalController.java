package com.meteor.admin.controller;

import com.meteor.admin.service.IMqFailMsgService;
import com.meteor.api.constants.MqFailMsgConstants;
import com.meteor.api.model.AdminMqFailureEntity;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  管理员内部接口
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 10:28
 */
@RestController
@RequestMapping("/internal/admin/mqFailures")
@RequiredArgsConstructor
public class AdminInternalController {
    private final  IMqFailMsgService adminMqFailureService;

    /**
     * 查询近 6.5 分钟内的失败/待处理消息
     *
     * @return 失败消息的列表
     */
    @Operation(summary = "内部-查询最近 6.5 分钟内失败（FAILED）/待处理（PENDING）的消息")
    @GetMapping("/recentFailed")
    public List<AdminMqFailureEntity> getRecentFailedMessages() {
        return adminMqFailureService.getRecentFailedMessages(MqFailMsgConstants.DEFAULT_PAST_MINUTES);
    }
}

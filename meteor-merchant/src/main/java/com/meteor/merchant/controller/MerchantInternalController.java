package com.meteor.merchant.controller;

import com.meteor.api.constants.MqFailMsgConstants;
import com.meteor.api.model.MerchantMqFailureEntity;
import com.meteor.merchant.service.IMerchantMqFailMsgService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  商家内部接口
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 10:07
 */
@RestController
@RequestMapping("/internal/merchant/mqFailures")
@RequiredArgsConstructor
public class MerchantInternalController {

    private final IMerchantMqFailMsgService merchantMqFailureService;

    /**
     * 查询近 6.5 分钟内的失败消息（内部接口直接返回实体即可）
     *
     * @return 失败消息的列表
     */
    @GetMapping("/recentFailed")
    @Operation(summary = "内部-查询最近 6.5 分钟内失败（FAILED）/待处理（PENDING）的消息")
    public List<MerchantMqFailureEntity> getRecentFailedMessages() {
        return merchantMqFailureService.getRecentFailedMessages(MqFailMsgConstants.DEFAULT_PAST_MINUTES);
    }
}

package com.meteor.analytics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.analytics.domain.entity.OpMqFailMsg;
import com.meteor.analytics.enums.SendState;

import java.time.LocalDateTime;
import java.util.List;

public interface IOpMqFailMsgService extends IService<OpMqFailMsg> {

    /**
     * 按唯一键 (source_module, msg_id) upsert
     * - 存在：更新业务字段 + collect_time=NOW() + collect_version+1
     */
    void upsertByUk(OpMqFailMsg e);

    /**
     * 一键全量补发（无条件）
     */
    ResendSummary resendAll();

    /**
     * 补发单条（方便排障/后续扩展）
     */
    ResendOneResult resendOne(Long id);

    List<PendingMqMsgDTO> listPendingMqMsgs(Integer doingTimeoutMinutes, Integer limit);

    record PendingMqMsgDTO(
            Long id,
            String sourceModule,
            String msgId,
            Long bizId,
            String exchangeName,
            String routingKey,
            String topic,
            Integer status,
            Integer retryCnt,
            LocalDateTime nextRetryTime,
            String lastError,
            LocalDateTime sourceCreateTime,
            LocalDateTime sourceUpdateTime,
            SendState resendState,
            String resendRequestId,
            Integer resendAttemptCnt,
            LocalDateTime resendLastTime,
            String resendLastError,
            LocalDateTime collectTime,
            Long collectVersion,
            LocalDateTime createTime,
            LocalDateTime updateTime
    ) {}


    /**
     * 补发结果汇总
     */
    record ResendSummary(
            String requestId,
            long totalCandidates,
            long locked,
            long success,
            long failed,
            java.util.List<ResendOneResult> skipped
    ) {}

    /**
     * 补发单条结果
     */
    record ResendOneResult(
            String requestId,
            Long id,
            String msgId,
            boolean locked,
            boolean success,
            String error
    ) {}
}

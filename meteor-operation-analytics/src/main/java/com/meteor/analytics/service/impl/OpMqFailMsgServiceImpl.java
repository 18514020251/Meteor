package com.meteor.analytics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.analytics.domain.entity.OpMqFailMsg;
import com.meteor.analytics.enums.SendState;
import com.meteor.analytics.mapper.OpMqFailMsgMapper;
import com.meteor.analytics.service.IOpMqFailMsgService;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

// NOTE: 提 MQ 操作为类、Assembler、拆私有方法
@Service
@RequiredArgsConstructor
@Slf4j
public class OpMqFailMsgServiceImpl extends ServiceImpl<OpMqFailMsgMapper, OpMqFailMsg>
        implements IOpMqFailMsgService {

    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final MqSender mqSender;
    private final ObjectMapper objectMapper;
    private static final Duration RESEND_TIMEOUT = Duration.ofSeconds(3);

    private static final String SQL_SET_RESEND_LAST_TIME_NOW = "resend_last_time = NOW()";
    private static final String SQL_INC_RESEND_ATTEMPT =
            "resend_attempt_cnt = resend_attempt_cnt + 1";

    private static final String SQL_COLLECT_TIME_NOW = "collect_time = NOW()";
    private static final String SQL_INC_COLLECT_VERSION = "collect_version = collect_version + 1";


    private static final int DOING_TIMEOUT_MINUTES = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertByUk(OpMqFailMsg e) {
        if (e == null) {
            throw new IllegalArgumentException("OpMqFailMsg must not be null");
        }

        log.info("[upsertByUk] sourceModule={}, msgId={}, bizId={}, exchangeName={}, routingKey={}, topic={}, status={}, retryCnt={}, lastErrorLength={}",
                e.getSourceModule(), e.getMsgId(), e.getBizId(), e.getExchangeName(),
                e.getRoutingKey(), e.getTopic(), e.getStatus(), e.getRetryCnt(), lastErrorLength(e));

        int updated = doUpdateByUk(e);
        log.info("[upsertByUk] updateByUk result: updated={}", updated);
        if (updated > 0) {
            return;
        }

        prepareForInsert(e);

        try {
            this.baseMapper.insert(e);
            log.info("[upsertByUk] insert success: sourceModule={}, msgId={}", e.getSourceModule(), e.getMsgId());
        } catch (DuplicateKeyException dup) {
            log.warn("[upsertByUk] insert duplicate key, fallback to update: sourceModule={}, msgId={}",
                    e.getSourceModule(), e.getMsgId(), dup);

            int retryUpdated = doUpdateByUk(e);
            log.info("[upsertByUk] retry updateByUk result: updated={}", retryUpdated);
        }
    }

    private int doUpdateByUk(OpMqFailMsg e) {
        return this.baseMapper.update(null, buildUpdateByUkWrapper(e));
    }

    private LambdaUpdateWrapper<OpMqFailMsg> buildUpdateByUkWrapper(OpMqFailMsg e) {
        return new LambdaUpdateWrapper<OpMqFailMsg>()
                .eq(OpMqFailMsg::getSourceModule, e.getSourceModule())
                .eq(OpMqFailMsg::getMsgId, e.getMsgId())

                .set(OpMqFailMsg::getBizId, e.getBizId())
                .set(OpMqFailMsg::getExchangeName, e.getExchangeName())
                .set(OpMqFailMsg::getRoutingKey, e.getRoutingKey())
                .set(OpMqFailMsg::getTopic, e.getTopic())
                .set(OpMqFailMsg::getPayload, e.getPayload())

                .set(OpMqFailMsg::getStatus, e.getStatus())
                .set(OpMqFailMsg::getRetryCnt, e.getRetryCnt())
                .set(OpMqFailMsg::getNextRetryTime, e.getNextRetryTime())
                .set(OpMqFailMsg::getLastError, e.getLastError())

                .set(OpMqFailMsg::getSourceCreateTime, e.getSourceCreateTime())
                .set(OpMqFailMsg::getSourceUpdateTime, e.getSourceUpdateTime())

                .setSql(SQL_COLLECT_TIME_NOW)
                .setSql(SQL_INC_COLLECT_VERSION);
    }

    private void prepareForInsert(OpMqFailMsg e) {
        e.setCollectTime(null);
        e.setCollectVersion(null);
    }

    private int lastErrorLength(OpMqFailMsg e) {
        String s = e.getLastError();
        return s == null ? 0 : s.length();
    }



    @Override
    public ResendSummary resendAll() {

        String batchRequestId = String.valueOf(snowflakeIdGenerator.nextId());

        List<Long> ids = listNeedResendIds();

        long total = ids.size();
        long lockedCnt = 0;
        long successCnt = 0;
        long failedCnt = 0;

        List<ResendOneResult> skippedSamples = new ArrayList<>(20);

        for (Long id : ids) {
            ResendOneResult r = resendOne(id);

            if (!r.locked()) {
                if (skippedSamples.size() < 20) {
                    skippedSamples.add(r);
                }
                continue;
            }

            lockedCnt++;

            if (r.success()) {
                successCnt++;
            } else {
                failedCnt++;
            }
        }

        return new ResendSummary(batchRequestId, total, lockedCnt, successCnt, failedCnt, skippedSamples);
    }

    private List<Long> listNeedResendIds() {
        LambdaQueryWrapper<OpMqFailMsg> qw = new LambdaQueryWrapper<>();

        qw.in(OpMqFailMsg::getResendState, SendState.WAIT, SendState.FAILED);

        qw.or(w -> w.eq(OpMqFailMsg::getResendState, SendState.DOING)
                .apply("resend_last_time is not null and resend_last_time < DATE_SUB(NOW(), INTERVAL {0} MINUTE)",
                        DOING_TIMEOUT_MINUTES));

        qw.select(OpMqFailMsg::getId);

        List<OpMqFailMsg> rows = list(qw);
        List<Long> ids = new ArrayList<>(rows.size());
        for (OpMqFailMsg r : rows) {
            ids.add(r.getId());
        }
        return ids;
    }



    @Override
    public ResendOneResult resendOne(Long id) {
        String requestId = String.valueOf(snowflakeIdGenerator.nextId());

        boolean locked = tryLockForResend(id, requestId);
        if (!locked) {
            OpMqFailMsg msgRecord = getById(id);
            return new ResendOneResult(
                    requestId,
                    id,
                    msgRecord == null ? null : msgRecord.getMsgId(),
                    false,
                    false,
                    "not locked (state not allowed / already doing / record not found)"
            );
        }

        OpMqFailMsg msgRecord = getById(id);
        if (msgRecord == null) {
            markFailed(id, "record not found after locked");
            return new ResendOneResult(requestId, id, null, true, false, "record not found");
        }

        if (isBlank(msgRecord.getExchangeName())) {
            String err = "exchange_name is blank";
            markFailed(id, err);
            return new ResendOneResult(requestId, id, msgRecord.getMsgId(), true, false, err);
        }
        if (isBlank(msgRecord.getRoutingKey())) {
            String err = "routing_key is blank";
            markFailed(id, err);
            return new ResendOneResult(requestId, id, msgRecord.getMsgId(), true, false, err);
        }

        try {
            MqSendResult result = sendToMqWithConfirm(msgRecord);
            boolean ok = result != null && result.isAck() && !result.noRoute();
            if (ok) {
                markSuccess(id);
                return new ResendOneResult(requestId, id, msgRecord.getMsgId(), true, true, null);
            }

            String err = buildSendError(result);
            markFailed(id, err);
            return new ResendOneResult(requestId, id, msgRecord.getMsgId(), true, false, err);

        } catch (Exception ex) {
            String err = safeErr(ex);
            markFailed(id, err);
            return new ResendOneResult(requestId, id, msgRecord.getMsgId(), true, false, err);
        }
    }


    /**
     * 尝试锁定：WAIT/FAILED -> DOING
     */
    private boolean tryLockForResend(Long id, String requestId) {

        int doingTimeoutMinutes = 5;

        return lambdaUpdate()
                .eq(OpMqFailMsg::getId, id)
                .and(w -> w
                        .in(OpMqFailMsg::getResendState, SendState.WAIT, SendState.FAILED)
                        .or()
                        .eq(OpMqFailMsg::getResendState, SendState.DOING)
                        .apply(
                                "resend_last_time is not null and resend_last_time < DATE_SUB(NOW(), INTERVAL {0} MINUTE)",
                                doingTimeoutMinutes
                        )
                )
                .set(OpMqFailMsg::getResendState, SendState.DOING)
                .set(OpMqFailMsg::getResendRequestId, requestId)
                .setSql(SQL_SET_RESEND_LAST_TIME_NOW)
                .update();
    }


    private String buildSendError(MqSendResult r) {
        if (r == null) return "MQ sendAndWaitConfirm returned null";

        if (r.noRoute()) {
            ReturnedMessage rm = r.getReturnedMessage();

            return truncate512(
                    "MQ_NO_ROUTE: replyCode=" + rm.getReplyCode()
                            + ", replyText=" + rm.getReplyText()
                            + ", exchange=" + rm.getExchange()
                            + ", routingKey=" + rm.getRoutingKey()
            );
        }

        if (!r.isAck()) {
            return truncate512("MQ_CONFIRM_FAILED: cause=" + r.getCause());
        }

        return "MQ_SEND_FAILED: unknown";
    }

    private void markSuccess(Long id) {
        lambdaUpdate()
                .eq(OpMqFailMsg::getId, id)
                .set(OpMqFailMsg::getResendState, SendState.SUCCESS)
                .set(OpMqFailMsg::getResendLastError, null)
                .setSql(SQL_INC_RESEND_ATTEMPT)
                .setSql(SQL_SET_RESEND_LAST_TIME_NOW)
                .update();
    }

    private void markFailed(Long id, String errMsg) {
        lambdaUpdate()
                .eq(OpMqFailMsg::getId, id)
                .set(OpMqFailMsg::getResendState, SendState.FAILED)
                .set(OpMqFailMsg::getResendLastError, truncate512(errMsg))
                .setSql(SQL_INC_RESEND_ATTEMPT)
                .setSql(SQL_SET_RESEND_LAST_TIME_NOW)
                .update();
    }

    private String safeErr(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) msg = ex.getClass().getSimpleName();
        return truncate512(msg);
    }

    private String truncate512(String s) {
        if (s == null) return null;
        return s.length() > 512 ? s.substring(0, 512) : s;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Object normalizePayload(String payload) {
        if (payload == null) return null;
        String s = payload.trim();
        if (s.isEmpty()) return null;

        try {
            return objectMapper.readTree(s);
        } catch (Exception e) {
            return s;
        }
    }

    private MqSendResult sendToMqWithConfirm(OpMqFailMsg r) {
        Object body = normalizePayload(r.getPayload());
        return mqSender.sendAndWaitConfirm(
                r.getExchangeName(),
                r.getRoutingKey(),
                body,
                RESEND_TIMEOUT
        );
    }

    @Override
    public List<PendingMqMsgDTO> listPendingMqMsgs(Integer doingTimeoutMinutes, Integer limit) {

        int timeoutMin = (doingTimeoutMinutes == null || doingTimeoutMinutes <= 0) ? 5 : doingTimeoutMinutes;
        int lim = (limit == null || limit <= 0) ? 500 : Math.min(limit, 5000);

        List<OpMqFailMsg> rows = baseMapper.selectPendingForResend(timeoutMin, lim);

        List<PendingMqMsgDTO> out = new ArrayList<>(rows.size());
        for (OpMqFailMsg r : rows) {
            out.add(new PendingMqMsgDTO(
                    r.getId(),
                    r.getSourceModule(),
                    r.getMsgId(),
                    r.getBizId(),
                    r.getExchangeName(),
                    r.getRoutingKey(),
                    r.getTopic(),
                    r.getStatus(),
                    r.getRetryCnt(),
                    r.getNextRetryTime(),
                    r.getLastError(),
                    r.getSourceCreateTime(),
                    r.getSourceUpdateTime(),
                    r.getResendState(),
                    r.getResendRequestId(),
                    r.getResendAttemptCnt(),
                    r.getResendLastTime(),
                    r.getResendLastError(),
                    r.getCollectTime(),
                    r.getCollectVersion(),
                    r.getCreateTime(),
                    r.getUpdateTime()
            ));
        }
        return out;
    }
}

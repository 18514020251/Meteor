package com.meteor.ticketing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.api.enums.GrabOrderResultEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.enums.ReservationReserveResult;
import com.meteor.ticketing.enums.ReservationTransitionResult;
import com.meteor.ticketing.mq.assmabler.MqOutboxEventAssembler;
import com.meteor.ticketing.mq.assmabler.TicketOrderMessageAssembler;
import com.meteor.ticketing.redis.GrabSemaphoreService;
import com.meteor.ticketing.service.IGrabOrderService;
import com.meteor.ticketing.service.IMqOutboxEventService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.service.idempotency.GrabRequestIdResolver;
import com.meteor.ticketing.service.reservation.ReservationReserveOutcome;
import com.meteor.ticketing.service.reservation.TicketReservationRedisService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 *  抢购订单服务
 *
 * @author Programmer
 * */
@RequiredArgsConstructor
@Service
@Slf4j
public class GrabOrderServiceImpl implements IGrabOrderService {

    private final SnowflakeIdGenerator idGenerator;
    private final ITicketingStockRedisService stockRedisService;
    private final IMqOutboxEventService outboxService;
    private final TicketOrderMessageAssembler assembler;
    private final ObjectMapper objectMapper;
    private final GrabSemaphoreService grabSemaphoreService;
    private final MqOutboxEventAssembler mqOutboxEventAssembler;
    private final GrabRequestIdResolver grabRequestIdResolver;
    private final TicketReservationRedisService reservationRedisService;

    private static final int BIZ_EXPIRE_MINUTES = 3;

    private static final String BIZ_GRAB_RESULT = "biz.grab_result";
    private static final String BIZ_SCREENING_ID = "biz.screening_id";
    private static final String BIZ_USER_ID = "biz.user_id";
    private static final String BIZ_ORDER_NO = "biz.order_no";
    private static final String BIZ_FAIL_REASON = "biz.fail_reason";
    private static final String BIZ_ROLLBACK_ERROR = "biz.rollback_error";
    private static final String BIZ_SEMAPHORE_RELEASE_ERROR = "biz.semaphore_release_error";
    private static final String BIZ_REQUEST_ID = "biz.request_id";
    private static final String BIZ_RESERVATION_ID = "biz.reservation_id";
    /* 抢购信号量租约 ttl 当前3秒为历史经验值,后续应根据 critical section 的 P99/P99.9 值进行调整 */
    private static final long GRAB_SEMAPHORE_LEASE_TTL_MS = 3000L;


    @Override
    public GrabOrderVO grab(Long screeningId, Long userId, String clientRequestId) {
        Span span = Span.current();
        span.setAttribute(BIZ_SCREENING_ID, String.valueOf(screeningId));
        span.setAttribute(BIZ_USER_ID, String.valueOf(userId));

        if (!stockRedisService.isSaleStarted(screeningId)) {
            span.setAttribute(BIZ_GRAB_RESULT, "NOT_READY");
            return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY);
        }

        String requestId = grabRequestIdResolver.resolve(userId, screeningId, clientRequestId, 1);

        span.setAttribute(BIZ_REQUEST_ID, requestId);
        span.setAttribute(BIZ_RESERVATION_ID, requestId);

        log.debug("[GrabIdentity] requestId={} reservationId={} screeningId={} userId={}",
                requestId, requestId, screeningId, userId);

        ReservationReserveOutcome reserveOutcome =
                reservationRedisService.reserve(requestId, screeningId, 1);

        ReservationReserveResult reserveResult = reserveOutcome.result();

        switch (reserveResult) {

            case RESERVED -> {
            }

            case IDEMPOTENT -> {
                span.setAttribute(BIZ_GRAB_RESULT, "BUSY");
                span.setAttribute(BIZ_FAIL_REASON, "RESERVATION_IDEMPOTENT");
                return GrabOrderVO.of(GrabOrderResultEnum.BUSY);
            }

            case SOLD_OUT -> {
                span.setAttribute(BIZ_GRAB_RESULT, "SOLD_OUT");
                return GrabOrderVO.of(GrabOrderResultEnum.SOLD_OUT);
            }

            case NOT_READY, NOT_STARTED, SALE_CLOSED -> {
                span.setAttribute(BIZ_GRAB_RESULT, "NOT_READY");
                return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY);
            }

            case INVALID_QUANTITY ->
                    throw new IllegalStateException("抢票预留数量必须为正数");
        }

        GrabSemaphoreService.Lease lease;
        try {
            lease = grabSemaphoreService.tryAcquire(screeningId, GRAB_SEMAPHORE_LEASE_TTL_MS);
        } catch (Exception acquireEx) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(acquireEx);
            span.setAttribute(BIZ_GRAB_RESULT, "FAILED");
            span.setAttribute(BIZ_FAIL_REASON, "SEMAPHORE_ACQUIRE_EXCEPTION");
            log.warn("[GrabSemaphoreAcquireFailed] requestId={} reservationId={} screeningId={}",
                    requestId, requestId, screeningId, acquireEx);
            compensateAfterAdmissionFailure(requestId, screeningId, span, "SEMAPHORE_ACQUIRE_EXCEPTION");
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "系统繁忙，请重试");
        }

        if (lease == null) {
            compensateAfterAdmissionFailure(requestId, screeningId, span, "SEMAPHORE_REJECT");
            span.setAttribute(BIZ_GRAB_RESULT, "BUSY");
            log.debug("[GrabSemaphoreRejected] requestId={} reservationId={} screeningId={}",
                    requestId, requestId, screeningId);
            return GrabOrderVO.of(GrabOrderResultEnum.BUSY);
        }
        String orderNo = String.valueOf(idGenerator.nextId());
        span.setAttribute(BIZ_ORDER_NO, orderNo);

        try {
            insertOutboxEvent(orderNo, userId, screeningId);

            span.setAttribute(BIZ_GRAB_RESULT, "SUCCESS");
            return GrabOrderVO.of(GrabOrderResultEnum.SUCCESS, orderNo, reserveOutcome.leftStock());

        } catch (Exception e) {
            try {
                ReservationTransitionResult compensationResult = reservationRedisService.compensate(requestId, screeningId);

                if (compensationResult != ReservationTransitionResult.APPLIED
                        && compensationResult != ReservationTransitionResult.IDEMPOTENT) {

                    span.setAttribute(BIZ_ROLLBACK_ERROR, "RESERVATION_COMPENSATE_" + compensationResult.name());
                }

            } catch (Exception compensationEx) {

                span.recordException(compensationEx);
                span.setAttribute(BIZ_ROLLBACK_ERROR, "RESERVATION_COMPENSATE_EXCEPTION");
            }

            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            span.setAttribute(BIZ_GRAB_RESULT, "FAILED");
            span.setAttribute(BIZ_FAIL_REASON, "OUTBOX_INSERT_FAIL");
            log.warn("[GrabOutboxInsertFailed] requestId={} reservationId={} screeningId={} orderNo={}",
                    requestId, requestId, screeningId, orderNo, e);

            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "系统繁忙，请重试");
        } finally {
            releaseSemaphoreSafely(screeningId, lease, requestId, span);
        }
    }

    private void compensateAfterAdmissionFailure(
            String reservationId,
            Long screeningId,
            Span span,
            String reason
    ) {
        try {
            ReservationTransitionResult compensationResult =
                    reservationRedisService.compensate(reservationId, screeningId);

            if (compensationResult == ReservationTransitionResult.APPLIED
                    || compensationResult == ReservationTransitionResult.IDEMPOTENT) {
                return;
            }

            span.setStatus(StatusCode.ERROR);
            span.setAttribute(BIZ_GRAB_RESULT, "FAILED");
            span.setAttribute(BIZ_FAIL_REASON, reason + "_COMPENSATE_" + compensationResult.name());
            log.error("[GrabReservationCompensateFailed] reason={} requestId={} reservationId={} screeningId={} result={}",
                    reason, reservationId, reservationId, screeningId, compensationResult);
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "系统繁忙，请重试");
        } catch (BizException e) {
            throw e;
        } catch (Exception compensationEx) {
            span.recordException(compensationEx);
            span.setStatus(StatusCode.ERROR);
            span.setAttribute(BIZ_ROLLBACK_ERROR, reason + "_COMPENSATE_EXCEPTION");
            log.error("[GrabReservationCompensateException] reason={} requestId={} reservationId={} screeningId={}",
                    reason, reservationId, reservationId, screeningId, compensationEx);
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "系统繁忙，请重试");
        }
    }

    private void releaseSemaphoreSafely(
            Long screeningId,
            GrabSemaphoreService.Lease lease,
            String reservationId,
            Span span
    ) {
        try {
            grabSemaphoreService.release(screeningId, lease.token());
        } catch (Exception releaseEx) {
            // lease 自身带 TTL，并有 GrabSemaphoreReclaimJob 兜底。
            // 释放失败不能覆盖已经落库的业务结果，否则会制造“业务成功但 API 报错”的不确定性。
            span.recordException(releaseEx);
            span.setAttribute(BIZ_SEMAPHORE_RELEASE_ERROR, true);
            log.warn("[GrabSemaphoreReleaseFailed] requestId={} reservationId={} screeningId={} token={}",
                    reservationId, reservationId, screeningId, lease.token(), releaseEx);
        }
    }


    /**
     * 写 Outbox 事件
     */
    private void insertOutboxEvent(String orderNo, Long userId, Long screeningId)
            throws JsonProcessingException {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusMinutes(BIZ_EXPIRE_MINUTES);

        TicketOrderCreateMessage message =
                assembler.from(orderNo, userId, screeningId);

        String payload = objectMapper.writeValueAsString(message);

        MqOutboxEvent event = mqOutboxEventAssembler.buildTicketOrderCreate(
                orderNo,
                payload,
                now,
                expireAt
        );

        boolean saved = outboxService.save(event);
        if (!saved) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "Outbox insert failed");
        }
    }
}

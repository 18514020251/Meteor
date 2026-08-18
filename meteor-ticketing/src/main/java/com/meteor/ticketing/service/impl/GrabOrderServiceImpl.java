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
import com.meteor.ticketing.domain.entity.TicketInventoryReservation;
import com.meteor.ticketing.enums.ReservationReserveResult;
import com.meteor.ticketing.enums.ReservationStatus;
import com.meteor.ticketing.enums.ReservationTransitionResult;
import com.meteor.ticketing.mq.assmabler.MqOutboxEventAssembler;
import com.meteor.ticketing.mq.assmabler.TicketOrderMessageAssembler;
import com.meteor.ticketing.redis.GrabSemaphoreService;
import com.meteor.ticketing.service.IGrabOrderService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.service.idempotency.GrabRequestIdResolver;
import com.meteor.ticketing.service.reservation.ReservationReserveOutcome;
import com.meteor.ticketing.service.reservation.TicketReservationRedisService;
import com.meteor.ticketing.service.transaction.ReservationOutboxRollbackException;
import com.meteor.ticketing.service.transaction.ReservationOutboxTransactionService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 抢购订单服务
 *
 * @author 昭兮
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class GrabOrderServiceImpl implements IGrabOrderService {

    private final SnowflakeIdGenerator idGenerator;
    private final ITicketingStockRedisService stockRedisService;
    private final TicketOrderMessageAssembler assembler;
    private final ObjectMapper objectMapper;
    private final GrabSemaphoreService grabSemaphoreService;
    private final MqOutboxEventAssembler mqOutboxEventAssembler;
    private final GrabRequestIdResolver grabRequestIdResolver;
    private final TicketReservationRedisService reservationRedisService;
    private final ReservationOutboxTransactionService reservationOutboxTransactionService;

    private static final int BIZ_EXPIRE_MINUTES = 3;
    private static final long GRAB_SEMAPHORE_LEASE_TTL_MS = 3000L;

    private static final String BIZ_GRAB_RESULT = "biz.grab_result";
    private static final String BIZ_SCREENING_ID = "biz.screening_id";
    private static final String BIZ_USER_ID = "biz.user_id";
    private static final String BIZ_ORDER_NO = "biz.order_no";
    private static final String BIZ_FAIL_REASON = "biz.fail_reason";
    private static final String BIZ_ROLLBACK_ERROR = "biz.rollback_error";
    private static final String BIZ_SEMAPHORE_RELEASE_ERROR = "biz.semaphore_release_error";
    private static final String BIZ_REQUEST_ID = "biz.request_id";
    private static final String BIZ_RESERVATION_ID = "biz.reservation_id";

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


        ReservationReserveOutcome reserveOutcome = reservationRedisService.reserve(requestId, screeningId, 1);
        ReservationReserveResult reserveResult = reserveOutcome.result();

        switch (reserveResult) {
            case RESERVED -> { /* 继续进入主链 */ }
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
            case INVALID_QUANTITY -> throw new IllegalStateException("抢票预留数量必须为正数");
        }


        GrabSemaphoreService.Lease lease;
        try {
            lease = grabSemaphoreService.tryAcquire(screeningId, GRAB_SEMAPHORE_LEASE_TTL_MS);
        } catch (Exception acquireEx) {
            markFailure(span, acquireEx, "SEMAPHORE_ACQUIRE_EXCEPTION");
            log.warn("[GrabSemaphoreAcquireFailed] requestId={} reservationId={} screeningId={}",
                    requestId, requestId, screeningId, acquireEx);
            compensateReservationOrThrow(requestId, screeningId, span, "SEMAPHORE_ACQUIRE_EXCEPTION");
            throw systemBusy();
        }

        if (lease == null) {
            compensateReservationOrThrow(requestId, screeningId, span, "SEMAPHORE_REJECT");
            span.setAttribute(BIZ_GRAB_RESULT, "BUSY");
            span.setAttribute(BIZ_FAIL_REASON, "SEMAPHORE_REJECT");
            log.debug("[GrabSemaphoreRejected] requestId={} reservationId={} screeningId={}", requestId, requestId, screeningId);
            return GrabOrderVO.of(GrabOrderResultEnum.BUSY);
        }


        try {
            String orderNo = String.valueOf(idGenerator.nextId());
            span.setAttribute(BIZ_ORDER_NO, orderNo);

            TicketInventoryReservation reservation = buildReservation(requestId, clientRequestId, userId, screeningId);

            MqOutboxEvent outboxEvent;
            try {
                outboxEvent = buildOutboxEvent(orderNo, userId, screeningId);
            } catch (Exception prepareEx) {
                markFailure(span, prepareEx, "PERSISTENCE_PREPARE_FAIL");
                log.warn("[GrabPersistencePrepareFailed] requestId={} reservationId={} screeningId={} orderNo={}",
                        requestId, requestId, screeningId, orderNo, prepareEx);
                compensateReservationOrThrow(requestId, screeningId, span, "PERSISTENCE_PREPARE_FAIL");
                throw systemBusy();
            }


            try {
                reservationOutboxTransactionService.persist(reservation, outboxEvent);
            } catch (ReservationOutboxRollbackException rollbackEx) {

                markFailure(span, rollbackEx, "RESERVATION_OUTBOX_ROLLBACK");
                log.warn("[GrabPersistenceRolledBack] requestId={} reservationId={} screeningId={} orderNo={}",
                        requestId, requestId, screeningId, orderNo, rollbackEx);
                compensateReservationOrThrow(requestId, screeningId, span, "RESERVATION_OUTBOX_ROLLBACK");
                throw systemBusy();
            } catch (Exception unknownEx) {

                markFailure(span, unknownEx, "RESERVATION_OUTBOX_OUTCOME_UNKNOWN");
                log.error("[GrabPersistenceOutcomeUnknown] requestId={} reservationId={} screeningId={} orderNo={} redisCompensated=false",
                        requestId, requestId, screeningId, orderNo, unknownEx);
                throw systemBusy();
            }


            span.setAttribute(BIZ_GRAB_RESULT, "SUCCESS");
            return GrabOrderVO.of(GrabOrderResultEnum.SUCCESS, orderNo, reserveOutcome.leftStock());

        } finally {
            releaseSemaphoreSafely(screeningId, lease, requestId, span);
        }
    }

    /**
     * 构造 MySQL Reservation。
     */
    private TicketInventoryReservation buildReservation(
            String reservationId,
            String clientRequestId,
            Long userId,
            Long screeningId
    ) {
        return new TicketInventoryReservation()
                .setReservationId(reservationId)
                .setClientRequestId(clientRequestId)
                .setScreeningId(screeningId)
                .setUserId(userId)
                .setQuantity(1)
                .setStatus(ReservationStatus.PRE_RESERVED);
    }

    /**
     * 构造 Outbox Event。
     * BIZ_EXPIRE_MINUTES 是建单事件过期语义，不是 Reservation 的业务过期时间。
     */
    private MqOutboxEvent buildOutboxEvent(String orderNo, Long userId, Long screeningId) throws JsonProcessingException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime outboxExpireAt = now.plusMinutes(BIZ_EXPIRE_MINUTES);
        TicketOrderCreateMessage message = assembler.from(orderNo, userId, screeningId);
        String payload = objectMapper.writeValueAsString(message);
        return mqOutboxEventAssembler.buildTicketOrderCreate(orderNo, payload, now, outboxExpireAt);
    }

    /**
     * 对"已经可以确定必须回滚 Redis Reservation"的场景执行幂等补偿。
     * 允许：APPLIED、IDEMPOTENT，其他结果都认为补偿闭环失败。
     */
    private void compensateReservationOrThrow(String reservationId, Long screeningId, Span span, String reason) {
        try {
            ReservationTransitionResult compensationResult = reservationRedisService.compensate(reservationId, screeningId);
            if (compensationResult == ReservationTransitionResult.APPLIED
                    || compensationResult == ReservationTransitionResult.IDEMPOTENT) {
                return;
            }
            span.setStatus(StatusCode.ERROR);
            span.setAttribute(BIZ_ROLLBACK_ERROR, reason + "_COMPENSATE_" + compensationResult.name());
            log.error("[GrabReservationCompensateFailed] reason={} requestId={} reservationId={} screeningId={} result={}",
                    reason, reservationId, reservationId, screeningId, compensationResult);
            throw systemBusy();
        } catch (BizException e) {
            throw e;
        } catch (Exception compensationEx) {
            span.recordException(compensationEx);
            span.setStatus(StatusCode.ERROR);
            span.setAttribute(BIZ_ROLLBACK_ERROR, reason + "_COMPENSATE_EXCEPTION");
            log.error("[GrabReservationCompensateException] reason={} requestId={} reservationId={} screeningId={}",
                    reason, reservationId, reservationId, screeningId, compensationEx);
            throw systemBusy();
        }
    }

    /**
     * Semaphore release 不改变最终业务结果。
     * lease 自身有 TTL，并有 reclaim job 兜底。
     */
    private void releaseSemaphoreSafely(Long screeningId, GrabSemaphoreService.Lease lease, String reservationId, Span span) {
        try {
            grabSemaphoreService.release(screeningId, lease.token());
        } catch (Exception releaseEx) {
            span.recordException(releaseEx);
            span.setAttribute(BIZ_SEMAPHORE_RELEASE_ERROR, true);
            log.warn("[GrabSemaphoreReleaseFailed] requestId={} reservationId={} screeningId={} token={}",
                    reservationId, reservationId, screeningId, lease.token(), releaseEx);
        }
    }

    /**
     * 统一记录 Grab 主链失败信息。
     */
    private void markFailure(Span span, Exception exception, String reason) {
        span.setStatus(StatusCode.ERROR);
        span.recordException(exception);
        span.setAttribute(BIZ_GRAB_RESULT, "FAILED");
        span.setAttribute(BIZ_FAIL_REASON, reason);
    }

    private BizException systemBusy() {
        return new BizException(CommonErrorCode.SYSTEM_ERROR, "系统繁忙，请重试");
    }
}
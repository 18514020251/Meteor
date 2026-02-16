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
import com.meteor.ticketing.mq.assmabler.MqOutboxEventAssembler;
import com.meteor.ticketing.mq.assmabler.TicketOrderMessageAssembler;
import com.meteor.ticketing.redis.GrabSemaphoreService;
import com.meteor.ticketing.service.IGrabOrderService;
import com.meteor.ticketing.service.IMqOutboxEventService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.service.cache.model.RedisStockOpResult;
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

    private static final int BIZ_EXPIRE_MINUTES = 3;

    private static final String BIZ_GRAB_RESULT = "biz.grab_result";
    private static final String BIZ_SCREENING_ID = "biz.screening_id";
    private static final String BIZ_USER_ID = "biz.user_id";
    private static final String BIZ_STOCK_DECR_CODE = "biz.stock_decr_code";
    private static final String BIZ_ORDER_NO = "biz.order_no";
    private static final String BIZ_FAIL_REASON = "biz.fail_reason";
    private static final String BIZ_ROLLBACK_ERROR = "biz.rollback_error";

    @Override
    public GrabOrderVO grab(Long screeningId, Long userId) {

        Span span = Span.current();
        span.setAttribute(BIZ_SCREENING_ID, String.valueOf(screeningId));
        span.setAttribute(BIZ_USER_ID, String.valueOf(userId));

        if (!stockRedisService.isSaleStarted(screeningId)) {
            span.setAttribute(BIZ_GRAB_RESULT, "NOT_READY");
            return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY);
        }

        RedisStockOpResult r = stockRedisService.decrStock1(screeningId);
        span.setAttribute(BIZ_STOCK_DECR_CODE, r.code().name());

        switch (r.code()) {
            case SUCCESS -> { /* 成功 */}
            case SOLD_OUT -> {
                span.setAttribute(BIZ_GRAB_RESULT, "SOLD_OUT");
                return GrabOrderVO.of(GrabOrderResultEnum.SOLD_OUT);
            }
            case NOT_READY -> {
                span.setAttribute(BIZ_GRAB_RESULT, "NOT_READY");
                return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY);
            }
            default -> {
                span.setAttribute(BIZ_GRAB_RESULT, "FAIL");
                span.setAttribute(BIZ_FAIL_REASON, "REDIS_DECR_FAIL");
                return GrabOrderVO.of(GrabOrderResultEnum.FAIL);
            }
        }

        String orderNo = String.valueOf(idGenerator.nextId());
        span.setAttribute(BIZ_ORDER_NO, orderNo);

        GrabSemaphoreService.Lease lease = grabSemaphoreService.tryAcquire(screeningId, 3000);
        if (lease == null) {
            span.setAttribute(BIZ_GRAB_RESULT, "BUSY");
            return GrabOrderVO.of(GrabOrderResultEnum.BUSY);
        }

        try {
            insertOutboxEvent(orderNo, userId, screeningId);

            span.setAttribute(BIZ_GRAB_RESULT, "SUCCESS");
            return GrabOrderVO.of(GrabOrderResultEnum.SUCCESS, orderNo, r.left());

        } catch (Exception e) {

            try {
                stockRedisService.incrStockN(screeningId, 1);
            } catch (Exception rollbackEx) {
                span.recordException(rollbackEx);
                span.setAttribute(BIZ_ROLLBACK_ERROR, "INCR_EXCEPTION");
            }

            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            span.setAttribute(BIZ_GRAB_RESULT, "FAILED");
            span.setAttribute(BIZ_FAIL_REASON, "OUTBOX_INSERT_FAIL");

            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "系统繁忙，请重试");
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

package com.meteor.ticketing.service.impl;

import com.meteor.api.enums.GrabOrderResultEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.mq.publisher.TicketOrderEventPublisher;
import com.meteor.ticketing.service.IGrabOrderService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.service.cache.model.RedisStockOpResult;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class GrabOrderServiceImpl implements IGrabOrderService {

    private final TicketOrderEventPublisher publisher;
    private final SnowflakeIdGenerator idGenerator;
    private final ITicketingStockRedisService stockRedisService;

    private static final String ATTR_SCREENING_ID = "biz.screening_id";
    private static final String ATTR_USER_ID = "biz.user_id";
    private static final String ATTR_ORDER_NO = "biz.order_no";
    private static final String ATTR_STOCK_DECR_CODE = "biz.stock_decr_code";
    private static final String ATTR_STOCK_ROLLBACK_CODE = "biz.stock_rollback_code";
    private static final String ATTR_STOCK_LEFT_AFTER_ROLLBACK = "biz.stock_left_after_rollback";
    private static final String ATTR_GRAB_RESULT = "biz.grab_result";
    private static final String ATTR_FAIL_REASON = "biz.fail_reason";
    private static final String ATTR_ROLLBACK_ERROR = "biz.rollback_error";

    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final String RESULT_SOLD_OUT = "SOLD_OUT";
    private static final String RESULT_NOT_READY = "NOT_READY";
    private static final String RESULT_FAIL = "FAIL";
    private static final String RESULT_FAILED = "FAILED";

    private static final String REASON_REDIS_DECR_FAIL = "REDIS_DECR_FAIL";
    private static final String REASON_MQ_PUBLISH_FAIL = "MQ_PUBLISH_FAIL";
    private static final String ROLLBACK_INCR_EXCEPTION = "INCR_EXCEPTION";

    @Override
    public GrabOrderVO grab(Long screeningId, Long userId) {

        Span span = Span.current();
        span.setAttribute(ATTR_SCREENING_ID, String.valueOf(screeningId));
        span.setAttribute(ATTR_USER_ID, String.valueOf(userId));

        if (!stockRedisService.isSaleStarted(screeningId)) {
            span.setAttribute(ATTR_GRAB_RESULT, RESULT_NOT_READY);
            return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY);
        }

        RedisStockOpResult r = stockRedisService.decrStock1(screeningId);
        span.setAttribute(ATTR_STOCK_DECR_CODE, r.code().name());

        switch (r.code()) {
            case SUCCESS -> { /* 继续 */ }
            case SOLD_OUT -> {
                span.setAttribute(ATTR_GRAB_RESULT, RESULT_SOLD_OUT);
                return GrabOrderVO.of(GrabOrderResultEnum.SOLD_OUT);
            }
            case NOT_READY -> {
                span.setAttribute(ATTR_GRAB_RESULT, RESULT_NOT_READY);
                return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY);
            }
            default -> {
                span.setAttribute(ATTR_GRAB_RESULT, RESULT_FAIL);
                span.setAttribute(ATTR_FAIL_REASON, REASON_REDIS_DECR_FAIL);
                return GrabOrderVO.of(GrabOrderResultEnum.FAIL);
            }
        }

        String orderNo = String.valueOf(idGenerator.nextId());
        span.setAttribute(ATTR_ORDER_NO, orderNo);

        try {
            publisher.publishCreateOrThrow(orderNo, userId, screeningId);

            span.setAttribute(ATTR_GRAB_RESULT, RESULT_SUCCESS);
            return GrabOrderVO.of(GrabOrderResultEnum.SUCCESS, orderNo, r.left());

        } catch (Exception e) {
            RedisStockOpResult rollback = null;
            try {
                rollback = stockRedisService.incrStockN(screeningId, 1);
            } catch (Exception rollbackEx) {
                span.recordException(rollbackEx);
                span.setAttribute(ATTR_ROLLBACK_ERROR, ROLLBACK_INCR_EXCEPTION);
            }

            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            span.setAttribute(ATTR_GRAB_RESULT, RESULT_FAILED);
            span.setAttribute(ATTR_FAIL_REASON, REASON_MQ_PUBLISH_FAIL);

            if (rollback != null) {
                span.setAttribute(ATTR_STOCK_ROLLBACK_CODE, rollback.code().name());
                if (rollback.left() != null) {
                    span.setAttribute(ATTR_STOCK_LEFT_AFTER_ROLLBACK, rollback.left());
                }
            }

            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "系统繁忙，已回滚库存，请重试");
        }
    }
}

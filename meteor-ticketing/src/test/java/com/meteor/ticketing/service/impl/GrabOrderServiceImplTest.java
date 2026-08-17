package com.meteor.ticketing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.api.enums.GrabOrderResultEnum;
import com.meteor.common.exception.BizException;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.enums.ReservationReserveResult;
import com.meteor.ticketing.enums.ReservationTransitionResult;
import com.meteor.ticketing.mq.assmabler.MqOutboxEventAssembler;
import com.meteor.ticketing.mq.assmabler.TicketOrderMessageAssembler;
import com.meteor.ticketing.redis.GrabSemaphoreService;
import com.meteor.ticketing.service.IMqOutboxEventService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.service.idempotency.GrabRequestIdResolver;
import com.meteor.ticketing.service.reservation.ReservationReserveOutcome;
import com.meteor.ticketing.service.reservation.TicketReservationRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 抢票主链测试。
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-11
 */
@ExtendWith(MockitoExtension.class)
class GrabOrderServiceImplTest {

    private static final Long SCREENING_ID = 2001L;
    private static final Long USER_ID = 1001L;
    private static final String CLIENT_REQUEST_ID = "test-client-request-id";
    private static final String REQUEST_ID = "request-900001";
    private static final long GENERATED_ORDER_ID = 900001L;
    private static final String ORDER_NO = String.valueOf(GENERATED_ORDER_ID);

    @Mock private SnowflakeIdGenerator idGenerator;
    @Mock private ITicketingStockRedisService stockRedisService;
    @Mock private IMqOutboxEventService outboxService;
    @Mock private TicketOrderMessageAssembler assembler;
    @Mock private ObjectMapper objectMapper;
    @Mock private GrabSemaphoreService grabSemaphoreService;
    @Mock private MqOutboxEventAssembler mqOutboxEventAssembler;
    @Mock private GrabRequestIdResolver grabRequestIdResolver;
    @Mock private TicketReservationRedisService reservationRedisService;

    private GrabOrderServiceImpl grabOrderService;

    @BeforeEach
    void setUp() {
        grabOrderService = new GrabOrderServiceImpl(
                idGenerator,
                stockRedisService,
                outboxService,
                assembler,
                objectMapper,
                grabSemaphoreService,
                mqOutboxEventAssembler,
                grabRequestIdResolver,
                reservationRedisService
        );
    }


    @DisplayName("当场次未开始销售时应直接返回 NOT_READY")
    @Test
    void grabShouldReturnNotReadyWhenSaleNotStarted() {

        when(stockRedisService.isSaleStarted(SCREENING_ID)).thenReturn(false);

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.NOT_READY.getCode());
        assertThat(result.msg()).isEqualTo(GrabOrderResultEnum.NOT_READY.getMsg());
        assertThat(result.orderNo()).isNull();
        assertThat(result.leftStock()).isNull();

        // 连 requestId 都不应该创建
        verifyNoInteractions(grabRequestIdResolver, reservationRedisService);

        // M1B 后 Grab 主链禁止再裸扣库存
        verify(stockRedisService, never()).decrStock1(SCREENING_ID);
    }

    @DisplayName("当 Reservation 判断库存已售罄时应返回 SOLD_OUT")
    @Test
    void grabShouldReturnSoldOutWhenStockSoldOut() {

        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);

        when(reservationRedisService.reserve(REQUEST_ID, SCREENING_ID, 1))
                .thenReturn(new ReservationReserveOutcome(ReservationReserveResult.SOLD_OUT, null));

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.SOLD_OUT.getCode());
        assertThat(result.msg()).isEqualTo(GrabOrderResultEnum.SOLD_OUT.getMsg());
        assertThat(result.orderNo()).isNull();
        assertThat(result.leftStock()).isNull();

        // SOLD_OUT 后绝不能继续进入下游
        verify(idGenerator, never()).nextId();
        verify(grabSemaphoreService, never()).tryAcquire(eq(SCREENING_ID), anyLong());

        // 禁止旧库存扣减路径复活
        verify(stockRedisService, never()).decrStock1(SCREENING_ID);
    }

    @DisplayName("当抢票流程成功时应返回订单号和 Reservation 扣减后的剩余库存")
    @Test
    void grabShouldReturnSuccessWhenGrabSucceeds() throws Exception {

        Long leftStock = 8L;

        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);
        stubReserved(REQUEST_ID, leftStock);

        when(idGenerator.nextId()).thenReturn(GENERATED_ORDER_ID);

        GrabSemaphoreService.Lease lease = new GrabSemaphoreService.Lease("ttookkeenn", 9999L);
        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(lease);

        MqOutboxEvent event = prepareOutboxEvent(ORDER_NO, USER_ID, SCREENING_ID);
        when(outboxService.save(event)).thenReturn(true);

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.SUCCESS.getCode());
        assertThat(result.msg()).isEqualTo(GrabOrderResultEnum.SUCCESS.getMsg());
        assertThat(result.orderNo()).isEqualTo(ORDER_NO);
        assertThat(result.leftStock()).isEqualTo(leftStock);

        verify(outboxService).save(event);

        // 正常流程必须主动释放 Semaphore
        verify(grabSemaphoreService).release(SCREENING_ID, lease.token());

        // Reservation 已经负责库存副作用
        verify(stockRedisService, never()).decrStock1(SCREENING_ID);
        verify(stockRedisService, never()).increaseAvailableStock(SCREENING_ID, 1);
    }

    @DisplayName("当 Semaphore 拒绝请求且补偿成功时应返回 BUSY")
    @Test
    void grabShouldReturnBusyWhenSemaphoreRejected() {

        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);
        stubReserved(REQUEST_ID, 8L);

        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(null);

        when(reservationRedisService.compensate(REQUEST_ID, SCREENING_ID))
                .thenReturn(ReservationTransitionResult.APPLIED);

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.BUSY.getCode());
        assertThat(result.orderNo()).isNull();
        assertThat(result.leftStock()).isNull();

        verify(reservationRedisService).compensate(REQUEST_ID, SCREENING_ID);

        // Semaphore 都没拿到，Outbox 绝不能执行
        verify(outboxService, never()).save(any(MqOutboxEvent.class));
    }

    @DisplayName("Semaphore 获取异常时应按 reservationId 尝试补偿")
    @Test
    void grabShouldCompensateReservationWhenSemaphoreAcquireThrows() {
        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);
        stubReserved(REQUEST_ID, 8L);

        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong()))
                .thenThrow(new RuntimeException("mock semaphore redis failure"));
        when(reservationRedisService.compensate(REQUEST_ID, SCREENING_ID))
                .thenReturn(ReservationTransitionResult.APPLIED);

        BizException exception = assertThrows(
                BizException.class,
                () -> grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID)
        );

        assertThat(exception.getMessage()).isEqualTo("系统繁忙，请重试");
        verify(reservationRedisService).compensate(REQUEST_ID, SCREENING_ID);
        verify(outboxService, never()).save(any(MqOutboxEvent.class));
        verify(stockRedisService, never()).increaseAvailableStock(SCREENING_ID, 1);
    }

    @DisplayName("Semaphore 释放异常不应覆盖已经成功的抢票结果")
    @Test
    void grabShouldKeepSuccessWhenSemaphoreReleaseThrows() throws Exception {
        Long leftStock = 8L;
        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);
        stubReserved(REQUEST_ID, leftStock);

        when(idGenerator.nextId()).thenReturn(GENERATED_ORDER_ID);
        GrabSemaphoreService.Lease lease = new GrabSemaphoreService.Lease("ttookkeenn", 9999L);
        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(lease);

        MqOutboxEvent event = prepareOutboxEvent(ORDER_NO, USER_ID, SCREENING_ID);
        when(outboxService.save(event)).thenReturn(true);
        doThrow(new RuntimeException("mock semaphore release failure"))
                .when(grabSemaphoreService).release(SCREENING_ID, lease.token());

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.SUCCESS.getCode());
        assertThat(result.orderNo()).isEqualTo(ORDER_NO);
        assertThat(result.leftStock()).isEqualTo(leftStock);
        verify(outboxService).save(event);
        verify(grabSemaphoreService).release(SCREENING_ID, lease.token());
    }

    @DisplayName("Semaphore 拒绝时不得再使用裸库存 +1 补偿")
    @Test
    void grabShouldNotUseLegacyStockIncrementWhenSemaphoreRejected() {

        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);
        stubReserved(REQUEST_ID, 8L);

        when(reservationRedisService.compensate(REQUEST_ID, SCREENING_ID))
                .thenReturn(ReservationTransitionResult.APPLIED);

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.BUSY.getCode());

        // 新模型：PRE_RESERVED -> COMPENSATED，库存恢复由 Reservation Lua 原子完成
        verify(reservationRedisService).compensate(REQUEST_ID, SCREENING_ID);

        // 旧补偿方式禁止再出现
        verify(stockRedisService, never()).increaseAvailableStock(SCREENING_ID, 1);
        verify(stockRedisService, never()).decrStock1(SCREENING_ID);
    }

    @DisplayName("连续多个独立请求被 Semaphore 拒绝时每个 Reservation 都应单独补偿")
    @Test
    void grabShouldCompensateEveryIndependentReservationWhenSemaphoreRejected() {

        when(stockRedisService.isSaleStarted(SCREENING_ID)).thenReturn(true);

        when(grabRequestIdResolver.resolve(eq(USER_ID), eq(SCREENING_ID), anyString(), eq(1)))
                .thenAnswer(invocation -> {
                    String clientRequestId = invocation.getArgument(2, String.class);
                    return "request-" + clientRequestId;
                });

        when(reservationRedisService.reserve(anyString(), eq(SCREENING_ID), eq(1)))
                .thenReturn(new ReservationReserveOutcome(ReservationReserveResult.RESERVED, 9L));

        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(null);

        when(reservationRedisService.compensate(anyString(), eq(SCREENING_ID)))
                .thenReturn(ReservationTransitionResult.APPLIED);

        for (int i = 0; i < 100; i++) {
            String clientRequestId = "client-request-" + i;
            GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, clientRequestId);
            assertThat(result.code()).isEqualTo(GrabOrderResultEnum.BUSY.getCode());
        }

        verify(reservationRedisService, times(100)).reserve(anyString(), eq(SCREENING_ID), eq(1));
        verify(reservationRedisService, times(100)).compensate(anyString(), eq(SCREENING_ID));

        // 100 次失败也不允许出现任何裸库存恢复
        verify(stockRedisService, never()).increaseAvailableStock(SCREENING_ID, 1);
        verify(stockRedisService, never()).decrStock1(SCREENING_ID);
    }

    @DisplayName("场次已开售时应先解析稳定 requestId 再执行 Reservation 预留")
    @Test
    void grabShouldResolveRequestIdBeforeReservingStock() {

        String clientRequestId = "client-request-001";
        String requestId = "request-900001";

        when(stockRedisService.isSaleStarted(SCREENING_ID)).thenReturn(true);
        when(grabRequestIdResolver.resolve(USER_ID, SCREENING_ID, clientRequestId, 1))
                .thenReturn(requestId);

        // 用 SOLD_OUT 让流程在 reserve 后停止，避免引入 Semaphore / Outbox 噪声
        when(reservationRedisService.reserve(requestId, SCREENING_ID, 1))
                .thenReturn(new ReservationReserveOutcome(ReservationReserveResult.SOLD_OUT, null));

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, clientRequestId);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.SOLD_OUT.getCode());

        InOrder inOrder = inOrder(stockRedisService, grabRequestIdResolver, reservationRedisService);

        // 当前 Java 快速销售检查仍然保留
        inOrder.verify(stockRedisService).isSaleStarted(SCREENING_ID);

        // 必须先获得稳定身份
        inOrder.verify(grabRequestIdResolver).resolve(USER_ID, SCREENING_ID, clientRequestId, 1);

        // 再以 requestId 作为 reservationId 执行库存副作用
        inOrder.verify(reservationRedisService).reserve(requestId, SCREENING_ID, 1);

        verify(stockRedisService, never()).decrStock1(SCREENING_ID);
    }

    @DisplayName("稳定 requestId 应作为 reservationId 进入 Redis Reservation 预留")
    @Test
    void grabShouldReserveByResolvedRequestId() throws JsonProcessingException {

        String clientRequestId = "client-request-001";
        String requestId = "request-900001";

        stubResolvedRequest(clientRequestId, requestId);
        stubReserved(requestId, 9L);

        when(idGenerator.nextId()).thenReturn(GENERATED_ORDER_ID);

        GrabSemaphoreService.Lease lease = new GrabSemaphoreService.Lease("ttookkeenn", 9999L);
        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(lease);

        MqOutboxEvent event = prepareOutboxEvent(ORDER_NO, USER_ID, SCREENING_ID);
        when(outboxService.save(event)).thenReturn(true);

        grabOrderService.grab(SCREENING_ID, USER_ID, clientRequestId);

        // requestId = reservationId
        verify(reservationRedisService).reserve(requestId, SCREENING_ID, 1);

        // 旧裸扣库存已经退出 Grab 主链
        verify(stockRedisService, never()).decrStock1(SCREENING_ID);
    }

    @DisplayName("Reservation 预留成功但 Semaphore 拒绝时应按 reservationId 补偿")
    @Test
    void grabShouldCompensateReservationWhenSemaphoreRejected() {

        String clientRequestId = "client-request-001";
        String requestId = "request-900001";

        stubResolvedRequest(clientRequestId, requestId);
        stubReserved(requestId, 9L);

        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(null);

        when(reservationRedisService.compensate(requestId, SCREENING_ID))
                .thenReturn(ReservationTransitionResult.APPLIED);

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, clientRequestId);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.BUSY.getCode());

        verify(reservationRedisService).compensate(requestId, SCREENING_ID);
        verify(stockRedisService, never()).increaseAvailableStock(SCREENING_ID, 1);
        verify(outboxService, never()).save(any(MqOutboxEvent.class));
    }


    @DisplayName("当 Outbox save 返回 false 时应抛系统异常并按 reservationId 补偿")
    @Test
    void grabShouldThrowExceptionWhenOutboxInsertFails() throws JsonProcessingException {

        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);
        stubReserved(REQUEST_ID, 8L);

        when(idGenerator.nextId()).thenReturn(GENERATED_ORDER_ID);

        GrabSemaphoreService.Lease lease = new GrabSemaphoreService.Lease("ttookkeenn", 9999L);
        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(lease);

        MqOutboxEvent event = prepareOutboxEvent(ORDER_NO, USER_ID, SCREENING_ID);
        when(outboxService.save(event)).thenReturn(false);

        when(reservationRedisService.compensate(REQUEST_ID, SCREENING_ID))
                .thenReturn(ReservationTransitionResult.APPLIED);

        BizException exception = assertThrows(
                BizException.class,
                () -> grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID)
        );

        assertThat(exception.getMessage()).isEqualTo("系统繁忙，请重试");

        verify(outboxService).save(event);

        // 即使 Outbox 失败，已获取的 Semaphore lease 也必须 finally 释放
        verify(grabSemaphoreService).release(SCREENING_ID, lease.token());

        verify(reservationRedisService).compensate(REQUEST_ID, SCREENING_ID);

        // 新模型禁止裸库存 +1
        verify(stockRedisService, never()).increaseAvailableStock(SCREENING_ID, 1);
    }

    @DisplayName("当 Outbox save 直接抛异常时也应按 reservationId 补偿")
    @Test
    void grabShouldCompensateReservationWhenOutboxInsertFails() throws JsonProcessingException {

        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);
        stubReserved(REQUEST_ID, 8L);

        when(idGenerator.nextId()).thenReturn(GENERATED_ORDER_ID);

        GrabSemaphoreService.Lease lease = new GrabSemaphoreService.Lease("ttookkeenn", 9999L);
        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(lease);

        MqOutboxEvent event = prepareOutboxEvent(ORDER_NO, USER_ID, SCREENING_ID);
        when(outboxService.save(event)).thenThrow(new RuntimeException("mock outbox insert failure"));

        when(reservationRedisService.compensate(REQUEST_ID, SCREENING_ID))
                .thenReturn(ReservationTransitionResult.APPLIED);

        BizException exception = assertThrows(
                BizException.class,
                () -> grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID)
        );

        assertThat(exception.getMessage()).isEqualTo("系统繁忙，请重试");

        verify(grabSemaphoreService).release(SCREENING_ID, lease.token());

        verify(reservationRedisService).compensate(REQUEST_ID, SCREENING_ID);

        // 不允许留下 stock 已恢复但 Reservation 仍 PRE_RESERVED 的状态裂缝
        verify(stockRedisService, never()).increaseAvailableStock(SCREENING_ID, 1);
    }

    @DisplayName("Semaphore 拒绝时 Reservation 已补偿过也应幂等返回 BUSY")
    @Test
    void grabShouldReturnBusyWhenSemaphoreCompensationIsIdempotent() {

        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);
        stubReserved(REQUEST_ID, 8L);

        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(null);

        when(reservationRedisService.compensate(REQUEST_ID, SCREENING_ID))
                .thenReturn(ReservationTransitionResult.IDEMPOTENT);

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.BUSY.getCode());
        assertThat(result.orderNo()).isNull();
        assertThat(result.leftStock()).isNull();

        verify(reservationRedisService).compensate(REQUEST_ID, SCREENING_ID);

        // Reservation Lua 已保证重复补偿不会再次 +1，Grab 层更不能绕开做裸补偿
        verify(stockRedisService, never()).increaseAvailableStock(SCREENING_ID, 1);

        // Semaphore reject 后不能写 Outbox
        verify(outboxService, never()).save(any(MqOutboxEvent.class));
    }

    @DisplayName("Semaphore 拒绝时不应生成订单号")
    @Test
    void grabShouldNotGenerateOrderNoWhenSemaphoreRejected() {

        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);
        stubReserved(REQUEST_ID, 8L);

        when(grabSemaphoreService.tryAcquire(eq(SCREENING_ID), anyLong())).thenReturn(null);

        when(reservationRedisService.compensate(REQUEST_ID, SCREENING_ID))
                .thenReturn(ReservationTransitionResult.APPLIED);

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.BUSY.getCode());
        assertThat(result.orderNo()).isNull();

        // Semaphore 都没有获得，业务还没有进入订单创建阶段，因此不应该提前消费 orderId
        verify(idGenerator, never()).nextId();
        verify(reservationRedisService).compensate(REQUEST_ID, SCREENING_ID);
        verify(outboxService, never()).save(any(MqOutboxEvent.class));
    }

    @DisplayName("Reservation reserve 幂等重放时不应再次进入 Semaphore 和 Outbox")
    @Test
    void grabShouldStopDownstreamWhenReservationReserveIsIdempotent() {

        stubResolvedRequest(CLIENT_REQUEST_ID, REQUEST_ID);

        when(reservationRedisService.reserve(REQUEST_ID, SCREENING_ID, 1))
                .thenReturn(new ReservationReserveOutcome(ReservationReserveResult.IDEMPOTENT, null));

        GrabOrderVO result = grabOrderService.grab(SCREENING_ID, USER_ID, CLIENT_REQUEST_ID);

        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.BUSY.getCode());
        assertThat(result.orderNo()).isNull();
        assertThat(result.leftStock()).isNull();

        verify(grabSemaphoreService, never()).tryAcquire(eq(SCREENING_ID), anyLong());
        verify(idGenerator, never()).nextId();
        verify(outboxService, never()).save(any(MqOutboxEvent.class));

        verify(stockRedisService, never()).decrStock1(SCREENING_ID);
        verify(stockRedisService, never()).increaseAvailableStock(SCREENING_ID, 1);
    }


    /**
     * 准备：已开售 + clientRequestId -> stable requestId
     */
    private void stubResolvedRequest(String clientRequestId, String requestId) {

        when(stockRedisService.isSaleStarted(SCREENING_ID)).thenReturn(true);
        when(grabRequestIdResolver.resolve(USER_ID, SCREENING_ID, clientRequestId, 1))
                .thenReturn(requestId);
    }

    /**
     * 准备 Reservation 真正预留成功。
     */
    private void stubReserved(String requestId, Long leftStock) {

        when(reservationRedisService.reserve(requestId, SCREENING_ID, 1))
                .thenReturn(new ReservationReserveOutcome(ReservationReserveResult.RESERVED, leftStock));
    }

    /**
     * 准备 Outbox 写入前的消息组装链路。
     */
    private MqOutboxEvent prepareOutboxEvent(String orderNo, Long userId, Long screeningId)
            throws JsonProcessingException {

        TicketOrderCreateMessage message =
                new TicketOrderCreateMessage(orderNo, userId, screeningId, LocalDateTime.now());

        when(assembler.from(orderNo, userId, screeningId)).thenReturn(message);

        String jsonPayload = "{\"test\":true}";
        when(objectMapper.writeValueAsString(message)).thenReturn(jsonPayload);

        MqOutboxEvent event = new MqOutboxEvent();
        when(mqOutboxEventAssembler.buildTicketOrderCreate(
                eq(orderNo), eq(jsonPayload), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(event);

        return event;
    }
}
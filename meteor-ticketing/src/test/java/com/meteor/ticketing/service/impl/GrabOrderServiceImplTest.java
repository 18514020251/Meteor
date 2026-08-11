package com.meteor.ticketing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.api.enums.GrabOrderResultEnum;
import com.meteor.common.exception.BizException;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.mq.assmabler.MqOutboxEventAssembler;
import com.meteor.ticketing.mq.assmabler.TicketOrderMessageAssembler;
import com.meteor.ticketing.redis.GrabSemaphoreService;
import com.meteor.ticketing.service.IMqOutboxEventService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.enums.RedisStockResultEnum;
import com.meteor.ticketing.service.cache.model.RedisStockOpResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * 抢票链路测试类
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-11
 */
@ExtendWith(MockitoExtension.class)
class GrabOrderServiceImplTest {

    @Mock
    private SnowflakeIdGenerator idGenerator;

    @Mock
    private ITicketingStockRedisService stockRedisService;

    @Mock
    private IMqOutboxEventService outboxService;

    @Mock
    private TicketOrderMessageAssembler assembler;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private GrabSemaphoreService grabSemaphoreService;

    @Mock
    private MqOutboxEventAssembler mqOutboxEventAssembler;

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
                mqOutboxEventAssembler
        );
    }


    @DisplayName("当场次未开始销售时，抢票结果")
    @Test
    void grabShouldReturnNotReadyWhenSaleNotStarted() {
        // Arrange
        Long screeningId = 2001L;
        Long userId = 1001L;

        when(stockRedisService.isSaleStarted(screeningId))
                .thenReturn(false);

        // Act
        GrabOrderVO result = grabOrderService.grab(screeningId, userId);

        // Assert
        assertThat(result.code())
                .isEqualTo(GrabOrderResultEnum.NOT_READY.getCode());
        assertThat(result.msg())
                .isEqualTo(GrabOrderResultEnum.NOT_READY.getMsg());
        assertThat(result.orderNo()).isNull();
        assertThat(result.leftStock()).isNull();

        verify(stockRedisService, never())
                .decrStock1(screeningId);
    }

    @DisplayName("当库存已售罄时，抢票返回 SOLD_OUT")
    @Test
    void grabShouldReturnSoldOutWhenStockSoldOut() {
        // Arrange
        Long screeningId = 2001L;
        Long userId = 1001L;

        when(stockRedisService.isSaleStarted(screeningId))
                .thenReturn(true);

        RedisStockOpResult soldOutResult =
                new RedisStockOpResult(
                        RedisStockResultEnum.SOLD_OUT,
                        -1L
                );

        when(stockRedisService.decrStock1(screeningId))
                .thenReturn(soldOutResult);

        // Act
        GrabOrderVO result =
                grabOrderService.grab(screeningId, userId);

        // Assert
        assertThat(result.code())
                .isEqualTo(GrabOrderResultEnum.SOLD_OUT.getCode());

        assertThat(result.msg())
                .isEqualTo(GrabOrderResultEnum.SOLD_OUT.getMsg());

        assertThat(result.orderNo()).isNull();
        assertThat(result.leftStock()).isNull();

        verify(idGenerator, never()).nextId();
    }

    @DisplayName("当抢票流程成功时，返回订单号和剩余库存")
    @Test
    void grabShouldReturnSuccessWhenGrabSucceeds() throws Exception {
        // Arrange
        Long screeningId = 2001L;
        Long userId = 1001L;

        long generatedOrderId = 900001L;
        String orderNo = String.valueOf(generatedOrderId);
        Long leftStock = 8L;

        // 1. 已开售
        when(stockRedisService.isSaleStarted(screeningId)).thenReturn(true);

        // 2. Redis 扣库存成功
        // 2.1 构建扣减方法decrStock1的返回结果
        RedisStockOpResult decrStockResult =
                new RedisStockOpResult(
                        RedisStockResultEnum.SUCCESS,
                        leftStock
                );

        // 2.2 设置decrStock1的返回结果
        when(stockRedisService.decrStock1(screeningId))
                .thenReturn(decrStockResult);

        // 3. 生成订单号
        when(idGenerator.nextId()).thenReturn(generatedOrderId);


        // 4. Semaphore 获取成功
        // 4.1 构建tryAcquire的返回结果
        GrabSemaphoreService.Lease lease = new GrabSemaphoreService.Lease(
                "ttookkeenn",
                9999L
        );

        // 4.2 设置tryAcquire的返回结果
        when(grabSemaphoreService.tryAcquire(screeningId, 3000))
                .thenReturn(lease);


        // 5. Outbox 所需 message
        // 5.1 构建form返回结果
        TicketOrderCreateMessage msg = new TicketOrderCreateMessage(
                orderNo,
                userId,
                screeningId,
                LocalDateTime.now()
        );

        // 5.2 设置form方法返回山上
       when(assembler.from(orderNo,userId,screeningId)).thenReturn(msg);

        // 6. JSON payload
        String jsonPayload = "{\"test\":true}";

        when(objectMapper.writeValueAsString(msg)).thenReturn(jsonPayload);


        // 7. Outbox event
        MqOutboxEvent event = new MqOutboxEvent();

        when(mqOutboxEventAssembler.buildTicketOrderCreate(
                eq(orderNo),
                eq(jsonPayload),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(event);


        // 8. 保存成功
        when(outboxService.save(event)).thenReturn(true);

        // Act
        GrabOrderVO result =
                grabOrderService.grab(screeningId, userId);

        // Assert
        // SUCCESS
        assertThat(result.msg()).isEqualTo(GrabOrderResultEnum.SUCCESS.getMsg());

        // orderNo
        assertThat(result.orderNo()).isEqualTo(orderNo);

        // code
        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.SUCCESS.getCode());


        // leftStock
        assertThat(result.leftStock()).isEqualTo(leftStock);


        // Semaphore 一定释放
        verify(grabSemaphoreService).release(screeningId, lease.token());

        // Outbox 确实保存
        verify(outboxService).save(event);

    }

    @DisplayName("当 Semaphore 拒绝请求时，抢票返回 BUSY")
    @Test
    void grabShouldReturnBusyWhenSemaphoreRejected() {
        // Arrange
        Long screeningId = 2001L;
        Long userId = 1001L;

        Long leftStock = 8L;
        long generatedOrderId = 900001L;

        // 1. 已经开售
        when(stockRedisService.isSaleStarted(screeningId)).thenReturn(true);

        // 2. Redis 扣库存成功
        RedisStockOpResult decrStockResult =
                new RedisStockOpResult(
                        RedisStockResultEnum.SUCCESS,
                        leftStock
                );

        // 2.2 设置decrStock1的返回结果
        when(stockRedisService.decrStock1(screeningId))
                .thenReturn(decrStockResult);

        // 3. 生成订单号
        when(idGenerator.nextId()).thenReturn(generatedOrderId);


        // 4. Semaphore 拒绝
        when(grabSemaphoreService.tryAcquire(screeningId, 3000))
                .thenReturn(null);


        // Act
        GrabOrderVO result = grabOrderService.grab(screeningId, userId);


        // Assert
        // BUSY
        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.BUSY.getCode());


        // orderNo 是否应该返回？
        assertThat(result.orderNo()).isNull();


        // leftStock 是否应该返回？
        assertThat(result.leftStock()).isNull();


        // Redis 的 decrStock1 应该执行过
        verify(stockRedisService).decrStock1(screeningId);


        // Outbox 此时绝对不应该执行
        verify(outboxService, never()).save(any(MqOutboxEvent.class));

    }

    @DisplayName("当 Outbox 写入失败时，抢票抛出系统异常")
    @Test
    void grabShouldThrowExceptionWhenOutboxInsertFails() throws JsonProcessingException {
        // Arrange
        Long screeningId = 2001L;
        Long userId = 1001L;

        long generatedOrderId = 900001L;
        String orderNo = String.valueOf(generatedOrderId);
        Long leftStock = 8L;

        // 1. 已开售
        when(stockRedisService.isSaleStarted(screeningId)).thenReturn(true);

        // 2. Redis 扣库存成功
        // 2.1 构建扣减方法decrStock1的返回结果
        RedisStockOpResult decrStockResult =
                new RedisStockOpResult(
                        RedisStockResultEnum.SUCCESS,
                        leftStock
                );

        // 2.2 设置decrStock1的返回结果
        when(stockRedisService.decrStock1(screeningId))
                .thenReturn(decrStockResult);

        // 3. 生成订单号
        when(idGenerator.nextId()).thenReturn(generatedOrderId);


        // 4. Semaphore 获取成功
        // 4.1 构建tryAcquire的返回结果
        GrabSemaphoreService.Lease lease = new GrabSemaphoreService.Lease(
                "ttookkeenn",
                9999L
        );

        // 4.2 设置tryAcquire的返回结果
        when(grabSemaphoreService.tryAcquire(screeningId, 3000))
                .thenReturn(lease);


        // 5. Outbox 所需 message
        // 5.1 构建form返回结果
        TicketOrderCreateMessage msg = new TicketOrderCreateMessage(
                orderNo,
                userId,
                screeningId,
                LocalDateTime.now()
        );

        // 5.2 设置form方法返回山上
        when(assembler.from(orderNo,userId,screeningId)).thenReturn(msg);

        // 6. JSON payload
        String jsonPayload = "{\"test\":true}";

        when(objectMapper.writeValueAsString(msg)).thenReturn(jsonPayload);


        // 7. Outbox event
        MqOutboxEvent event = new MqOutboxEvent();

        when(mqOutboxEventAssembler.buildTicketOrderCreate(
                eq(orderNo),
                eq(jsonPayload),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(event);


        // 8. 保存失败
        when(outboxService.save(event)).thenReturn(false);

        BizException exception = assertThrows(
                BizException.class,
                () -> grabOrderService.grab(screeningId, userId)
        );

        assertThat(exception.getMessage())
                .isEqualTo("系统繁忙，请重试");


        verify(grabSemaphoreService).release(screeningId, lease.token());

    }

    @DisplayName("当 Semaphore 拒绝请求时，应恢复已扣减的 Redis 库存")
    @Test
    void grabShouldRestoreStockWhenSemaphoreRejected() {
        // Arrange
        Long screeningId = 2001L;
        Long userId = 1001L;

        // 已开售
        when(stockRedisService.isSaleStarted(screeningId)).thenReturn(true);

        // Redis 扣库存成功
        when(stockRedisService.decrStock1(screeningId))
                .thenReturn(new RedisStockOpResult(RedisStockResultEnum.SUCCESS, 8L));


        // 生成订单号
        when(idGenerator.nextId()).thenReturn(900001L);


        // Semaphore reject
        when(grabSemaphoreService.tryAcquire(screeningId, 3000)).thenReturn(null);

        // Act
        GrabOrderVO result = grabOrderService.grab(screeningId, userId);

        // Assert
        // 确实返回 BUSY
        assertThat(result.code()).isEqualTo(GrabOrderResultEnum.BUSY.getCode());

        // 必须恢复刚刚扣掉的 1 张库存
        verify(stockRedisService).incrStockN(screeningId, 1);
    }
}

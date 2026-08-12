package com.meteor.ticketing.service.impl;

import com.meteor.ticketing.mapper.ScreeningMapper;
import com.meteor.ticketing.service.hot.ScreeningHotCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 电影放映服务测试类
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-12
 */
@ExtendWith(MockitoExtension.class)
class ScreeningServiceImplTest {

    @Mock
    private HotRankServiceImpl hotRankService;

    @Mock
    private ScreeningHotCounter screeningHotCounter;

    @Mock
    private ScreeningMapper screeningMapper;

    private ScreeningServiceImpl screeningService;

    @BeforeEach
    void setUp() {
        screeningService = new ScreeningServiceImpl(
                hotRankService,
                screeningHotCounter
        );

        ReflectionTestUtils.setField(
                screeningService,
                "baseMapper",
                screeningMapper
        );
    }

    @DisplayName("扣减库存 SQL 更新一行时，应返回成功")
    @Test
    void decrStockShouldReturnTrueWhenOneRowUpdated() {
        // Arrange
        Long screeningId = 2001L;

        when(screeningMapper.decreaseAvailableAndIncreaseSold(
                eq(screeningId),
                eq(1),
                any(LocalDateTime.class)
        )).thenReturn(1);

        // Act
        boolean result = screeningService.decrStockAndIncrSold(screeningId);

        // Assert
        assertThat(result).isTrue();

        verify(screeningMapper)
                .decreaseAvailableAndIncreaseSold(
                        eq(screeningId),
                        eq(1),
                        any(LocalDateTime.class)
                );
    }

    @DisplayName("可售库存不足导致 SQL 未更新时，应返回失败")
    @Test
    void decrStockShouldReturnFalseWhenNoRowUpdated() {
        // Arrange
        Long screeningId = 2001L;

        when(screeningMapper.decreaseAvailableAndIncreaseSold(
                eq(screeningId),
                eq(1),
                any(LocalDateTime.class)
        )).thenReturn(0);

        // Act
        boolean result = screeningService.decrStockAndIncrSold(screeningId);

        // Assert
        assertThat(result).isFalse();
    }

    @DisplayName("释放库存 SQL 更新一行时，应返回成功")
    @Test
    void releaseStockShouldReturnTrueWhenOneRowUpdated() {
        // Arrange
        Long screeningId = 2001L;
        Integer quantity = 1;

        when(screeningMapper.increaseAvailableAndDecreaseSold(
                eq(screeningId),
                eq(quantity),
                any(LocalDateTime.class)
        )).thenReturn(1);

        // Act
        boolean result = screeningService.incrStockAndDecrSold(screeningId, quantity);

        // Assert
        assertThat(result).isTrue();

        verify(screeningMapper)
                .increaseAvailableAndDecreaseSold(
                        eq(screeningId),
                        eq(quantity),
                        any(LocalDateTime.class)
                );
    }

    @DisplayName("已售库存不足导致 SQL 未更新时，应返回失败")
    @Test
    void releaseStockShouldReturnFalseWhenNoRowUpdated() {
        // Arrange
        Long screeningId = 2001L;
        Integer quantity = 1;

        when(screeningMapper.increaseAvailableAndDecreaseSold(
                eq(screeningId),
                eq(quantity),
                any(LocalDateTime.class)
        )).thenReturn(0);

        // Act
        boolean result = screeningService.incrStockAndDecrSold(screeningId, quantity);

        // Assert
        assertThat(result).isFalse();
    }

    @DisplayName("释放数量非法时，不应执行库存更新")
    @Test
    void releaseStockShouldRejectNonPositiveQuantity() {
        // Act
        boolean result = screeningService.incrStockAndDecrSold(2001L, -1);

        // Assert
        assertThat(result).isFalse();

        verifyNoInteractions(screeningMapper);
    }
}

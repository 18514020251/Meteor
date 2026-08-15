package com.meteor.ticketing.job;

import com.meteor.api.enums.ScreeningStatusEnum;
import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.ticketing.domain.entity.Screening;
import com.meteor.ticketing.mapper.ScreeningMapper;
import com.meteor.ticketing.service.warmup.ScreeningStockWarmupService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *  场次库存预热任务 Redis 集成测试
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-14
 */
class ScreeningStockWarmupServiceRedisIntegrationTest {

    private static final Long SCREENING_ID = 3001L;

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;

    private ScreeningMapper screeningMapper;
    private ScreeningStockWarmupService warmupService;

    @BeforeAll
    static void initRedis() {
        String host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");

        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));

        String password = System.getenv().getOrDefault("REDIS_PASSWORD", "");

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);

        configuration.setDatabase(15);

        if (!password.isBlank()) {
            configuration.setPassword(RedisPassword.of(password));
        }

        connectionFactory = new LettuceConnectionFactory(configuration);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
    }

    @BeforeEach
    void setUp() {
        screeningMapper = mock(ScreeningMapper.class);
        warmupService = new ScreeningStockWarmupService(screeningMapper, redisTemplate);
    }

    @AfterEach
    void cleanRedis() {
        redisTemplate.delete(List.of(
                RedisKeyConstants.buildScreeningStockKey(SCREENING_ID),
                RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID),
                RedisKeyConstants.buildScreeningStockWarmLockKey(SCREENING_ID),
                RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID),
                RedisKeyConstants.buildGrabSemMaxKey(SCREENING_ID),
                RedisKeyConstants.buildGrabSemPermitsKey(SCREENING_ID),
                RedisKeyConstants.buildGrabSemLeaseZsetKey(SCREENING_ID)
        ));

        redisTemplate.delete(RedisKeyConstants.grabActiveScreeningZsetKey());
    }

    @AfterAll
    static void closeRedis() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @DisplayName("场次预热时应写入 saleEndKey 并设置正确停售时间与 TTL")
    @Test
    void warmOneShouldPersistSaleEndKeyWithTtl() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime saleStartTime = now.plusMinutes(1);
        LocalDateTime saleEndTime = now.plusMinutes(10);
        LocalDateTime startTime = now.plusHours(1);

        Screening screening = new Screening()
                .setId(SCREENING_ID)
                .setSaleStartTime(saleStartTime)
                .setSaleEndTime(saleEndTime)
                .setStartTime(startTime)
                .setAvailableTickets(100)
                .setStatus(ScreeningStatusEnum.SCHEDULED)
                .setDeleted(DeleteStatus.NORMAL);

        when(screeningMapper.selectById(SCREENING_ID)).thenReturn(screening);

        warmupService.warmOne(SCREENING_ID, now);

        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String actualSaleEndEpoch = redisTemplate.opsForValue().get(saleEndKey);

        long expectedSaleEndEpoch = saleEndTime
                .atZone(ZoneId.systemDefault())
                .toEpochSecond();

        assertThat(actualSaleEndEpoch).isEqualTo(String.valueOf(expectedSaleEndEpoch));

        Long actualTtlSeconds = redisTemplate.getExpire(saleEndKey, TimeUnit.SECONDS);

        assertThat(actualTtlSeconds)
                .isNotNull()
                .isPositive();

        long expectedTtlSeconds = Duration.between(now, saleEndTime.plus(RedisKeyConstants.EXTRA_TTL))
                .getSeconds();

        assertThat(actualTtlSeconds).isBetween(expectedTtlSeconds - 5, expectedTtlSeconds);
    }
}

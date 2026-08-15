package com.meteor.ticketing.redis;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.ticketing.enums.ReservationReserveResult;
import com.meteor.ticketing.service.reservation.TicketReservationRedisService;
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
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ticket Reservation Redis 集成测试。
 *
 * 目标：
 * 验证 Reservation Lua 对库存修改和 Reservation 状态登记的原子语义。
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-15
 */
class TicketReservationRedisIntegrationTest {

    private static final Long SCREENING_ID = 2001L;

    private static final String RESERVATION_ID = "request-900001";

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;
    private TicketReservationRedisService reservationRedisService;

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
        long saleEndEpoch = Instant.now().getEpochSecond() + 60;
        prepareSaleWindow(SCREENING_ID, saleEndEpoch);
        reservationRedisService = new TicketReservationRedisService(redisTemplate);
    }

    @AfterEach
    void cleanRedis() {
        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = buildReservationKey(RESERVATION_ID);

        redisTemplate.delete(List.of(stockKey, readyKey, saleEndKey, reservationKey));
    }

    @AfterAll
    static void closeRedis() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @DisplayName("同一 reservationId 重复 reserve 只应扣减一次库存")
    @Test
    void sameReservationIdShouldReserveStockOnlyOnce() {
        /*
         * Arrange
         */
        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = buildReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        /*
         * KEYS:
         *
         * 1 = stockKey
         * 2 = reservationKey
         * 3 = readyKey
         * 4 = saleEndKey
         *
         * ARGV:
         *
         * 1 = screeningId
         * 2 = quantity
         * 3 = reservation TTL(ms)
         */
        Long first = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1",
                "60000"
        );

        Long second = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        /*
         * Assert
         *
         * 1 = 本次真正完成 reserve
         * 2 = reservation 已存在，
         *     本次属于幂等重放
         */
        assertThat(first).isEqualTo(1L);
        assertThat(second).isEqualTo(2L);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");

        assertThat(redisTemplate.opsForHash().get(reservationKey, "status"))
                .isEqualTo("PRE_RESERVED");

        assertThat(redisTemplate.opsForHash().get(reservationKey, "screeningId"))
                .isEqualTo(String.valueOf(SCREENING_ID));

        assertThat(redisTemplate.opsForHash().get(reservationKey, "quantity"))
                .isEqualTo("1");
    }

    @DisplayName("PRE_RESERVED reservation 不应通过 TTL 静默过期")
    @Test
    void preReservedReservationShouldNotExpireSilently() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = buildReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        Long result = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(result).isEqualTo(1L);

        assertThat(redisTemplate.getExpire(reservationKey)).isEqualTo(-1L);
    }

    @DisplayName("停售后新的 reservation 不应扣减库存")
    @Test
    void newReservationShouldNotReserveAfterSaleClosed() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = buildReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        redisTemplate.opsForValue().set(
                saleEndKey,
                String.valueOf(Instant.now().getEpochSecond() - 1),
                Duration.ofMinutes(2)
        );

        Long result = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(result).isEqualTo(-5L);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("10");

        assertThat(redisTemplate.hasKey(reservationKey)).isFalse();
    }

    @DisplayName("同一 reservationId 重复 release 只应恢复一次库存")
    @Test
    void sameReservationIdShouldReleaseStockOnlyOnce() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = buildReservationKey(RESERVATION_ID);


        redisTemplate.opsForValue().set(stockKey, "10");


        Long reserveResult = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(reserveResult).isEqualTo(1L);
        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");


        Long firstRelease = redisTemplate.execute(
                RedisScripts.RELEASE_RESERVATION,
                List.of(stockKey, reservationKey),
                "RELEASED"
        );


        Long secondRelease = redisTemplate.execute(
                RedisScripts.RELEASE_RESERVATION,
                List.of(stockKey, reservationKey),
                "RELEASED"
        );


        assertThat(firstRelease).isEqualTo(1L);
        assertThat(secondRelease).isEqualTo(2L);


        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("10");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("RELEASED");
    }

    @DisplayName("同一 reservationId 重复 confirm 只应确认一次且不再次扣减库存")
    @Test
    void sameReservationIdShouldConfirmOnlyOnce() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = buildReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        /*
         * stock 10 -> 9
         * status -> PRE_RESERVED
         */
        Long reserveResult = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(reserveResult).isEqualTo(1L);
        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");

        /*
         * PRE_RESERVED -> CONFIRMED
         */
        Long firstConfirm = redisTemplate.execute(
                RedisScripts.CONFIRM_RESERVATION,
                List.of(reservationKey)
        );

        /*
         * CONFIRMED -> CONFIRMED
         * 应该是幂等重放。
         */
        Long secondConfirm = redisTemplate.execute(
                RedisScripts.CONFIRM_RESERVATION,
                List.of(reservationKey)
        );

        /*
         * 1 = 本次真正完成确认
         * 2 = 已经 CONFIRMED，幂等重放
         */
        assertThat(firstConfirm).isEqualTo(1L);
        assertThat(secondConfirm).isEqualTo(2L);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("CONFIRMED");
    }

    @DisplayName("CONFIRMED reservation 不允许 release 且库存不得恢复")
    @Test
    void confirmedReservationShouldRejectRelease() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = buildReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        Long reserveResult = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(reserveResult).isEqualTo(1L);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");

        Long confirmResult = redisTemplate.execute(
                RedisScripts.CONFIRM_RESERVATION,
                List.of(reservationKey)
        );

        assertThat(confirmResult).isEqualTo(1L);
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("CONFIRMED");

        Long releaseResult = redisTemplate.execute(
                RedisScripts.RELEASE_RESERVATION,
                List.of(stockKey, reservationKey),
                "RELEASED"
        );

        /*
         * -3 = 当前状态不允许 release
         */
        assertThat(releaseResult).isEqualTo(-3L);

        /*
         * CONFIRMED 后 release 被拒绝，
         * 库存绝不能从 9 恢复到 10。
         */
        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("CONFIRMED");
    }

    @DisplayName("Reservation Redis Service 应返回明确的 reserve 业务结果")
    @Test
    void reservationServiceShouldReturnExplicitReserveResult() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        ReservationReserveResult first =
                reservationRedisService.reserve(RESERVATION_ID, SCREENING_ID, 1);

        ReservationReserveResult second =
                reservationRedisService.reserve(RESERVATION_ID, SCREENING_ID, 1);

        assertThat(first).isEqualTo(ReservationReserveResult.RESERVED);
        assertThat(second).isEqualTo(ReservationReserveResult.IDEMPOTENT);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");
    }


    /**
     * 准备有效销售窗口。
     *
     * readyKey 当前保存 saleStart epoch。
     *
     * @param screeningId 场次 ID
     * @param saleEndEpoch 销售截止时间 epoch 秒
     */
    private static void prepareSaleWindow(Long screeningId, long saleEndEpoch) {
        long nowEpoch = Instant.now().getEpochSecond();

        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(screeningId);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(screeningId);

        /*
         * saleStart = 当前时间前 60 秒，
         * 所以当前必然已经开售。
         */
        redisTemplate.opsForValue().set(readyKey, String.valueOf(nowEpoch - 60), Duration.ofMinutes(2));

        /*
         * saleEnd = 当前时间后 60 秒，
         * 所以当前必然尚未停售。
         */
        redisTemplate.opsForValue().set(saleEndKey, String.valueOf(saleEndEpoch), Duration.ofMinutes(2));
    }

    private static String buildReservationKey(String reservationId) {
        return "grab:reservation:" + reservationId;
    }
}
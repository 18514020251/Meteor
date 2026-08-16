package com.meteor.ticketing.redis;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.ticketing.enums.ReservationReserveResult;
import com.meteor.ticketing.enums.ReservationStatus;
import com.meteor.ticketing.enums.ReservationTransitionResult;
import com.meteor.ticketing.service.reservation.ReservationReserveOutcome;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ticket Reservation Redis 集成测试。
 *
 * <p>验证两层能力：
 *
 * <p>第一层：Redis Lua 原子协议
 *
 * <ul>
 *     <li>reserve 幂等扣减库存</li>
 *     <li>release 幂等恢复库存</li>
 *     <li>confirm 幂等状态转换</li>
 *     <li>非法终态转换被拒绝</li>
 * </ul>
 *
 * <p>第二层：TicketReservationRedisService Java 门面
 *
 * <ul>
 *     <li>隐藏 Lua 数字返回码</li>
 *     <li>转换为 Java 领域结果</li>
 *     <li>reserve 一次返回 result + leftStock</li>
 * </ul>
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
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

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

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        /*
         * 第一次：
         * stock 10 -> 9
         * Reservation -> PRE_RESERVED
         * 返回：{1, 9}
         */
        List<?> first = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        /*
         * 第二次：
         * Reservation 已经存在，不再次扣库存。
         * 返回：{2, -1}
         */
        List<?> second = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(scriptLong(first, 0)).isEqualTo(1L);
        assertThat(scriptLong(first, 1)).isEqualTo(9L);
        assertThat(scriptLong(second, 0)).isEqualTo(2L);
        assertThat(scriptLong(second, 1)).isEqualTo(-1L);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("PRE_RESERVED");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "screeningId")).isEqualTo(String.valueOf(SCREENING_ID));
        assertThat(redisTemplate.opsForHash().get(reservationKey, "quantity")).isEqualTo("1");
    }

    @DisplayName("PRE_RESERVED reservation 不应通过 TTL 静默过期")
    @Test
    void preReservedReservationShouldNotExpireSilently() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        List<?> result = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(scriptLong(result, 0)).isEqualTo(1L);
        assertThat(scriptLong(result, 1)).isEqualTo(9L);

        // -1：key 存在，但没有过期时间
        assertThat(redisTemplate.getExpire(reservationKey)).isEqualTo(-1L);
    }

    @DisplayName("停售后新的 reservation 不应扣减库存")
    @Test
    void newReservationShouldNotReserveAfterSaleClosed() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        // 强制设置为已经停售
        long closedSaleEnd = Instant.now().getEpochSecond() - 60;
        redisTemplate.opsForValue().set(saleEndKey, String.valueOf(closedSaleEnd), Duration.ofMinutes(2));

        // 验证测试数据已写入 Redis
        assertThat(redisTemplate.opsForValue().get(saleEndKey)).isEqualTo(String.valueOf(closedSaleEnd));

        List<?> result = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(scriptLong(result, 0)).isEqualTo(-5L);
        assertThat(scriptLong(result, 1)).isEqualTo(-1L);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("10");
        assertThat(redisTemplate.hasKey(reservationKey)).isFalse();
    }

    @DisplayName("同一 reservationId 重复 release 只应恢复一次库存")
    @Test
    void sameReservationIdShouldReleaseStockOnlyOnce() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        List<?> reserveResult = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(scriptLong(reserveResult, 0)).isEqualTo(1L);
        assertThat(scriptLong(reserveResult, 1)).isEqualTo(9L);

        Long firstRelease = redisTemplate.execute(
                RedisScripts.RELEASE_RESERVATION,
                List.of(stockKey, reservationKey),
                "RELEASED",
                String.valueOf(SCREENING_ID)
        );

        Long secondRelease = redisTemplate.execute(
                RedisScripts.RELEASE_RESERVATION,
                List.of(stockKey, reservationKey),
                "RELEASED",
                String.valueOf(SCREENING_ID)
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
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        List<?> reserveResult = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(scriptLong(reserveResult, 0)).isEqualTo(1L);
        assertThat(scriptLong(reserveResult, 1)).isEqualTo(9L);

        Long firstConfirm = redisTemplate.execute(
                RedisScripts.CONFIRM_RESERVATION,
                List.of(reservationKey)
        );

        Long secondConfirm = redisTemplate.execute(
                RedisScripts.CONFIRM_RESERVATION,
                List.of(reservationKey)
        );

        assertThat(firstConfirm).isEqualTo(1L);
        assertThat(secondConfirm).isEqualTo(2L);

        // confirm 只转换状态，不修改库存
        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("CONFIRMED");
    }

    @DisplayName("CONFIRMED reservation 不允许 release 且库存不得恢复")
    @Test
    void confirmedReservationShouldRejectRelease() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        List<?> reserveResult = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(SCREENING_ID),
                "1"
        );

        assertThat(scriptLong(reserveResult, 0)).isEqualTo(1L);
        assertThat(scriptLong(reserveResult, 1)).isEqualTo(9L);

        Long confirmResult = redisTemplate.execute(
                RedisScripts.CONFIRM_RESERVATION,
                List.of(reservationKey)
        );

        assertThat(confirmResult).isEqualTo(1L);
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("CONFIRMED");

        Long releaseResult = redisTemplate.execute(
                RedisScripts.RELEASE_RESERVATION,
                List.of(stockKey, reservationKey),
                "RELEASED",
                String.valueOf(SCREENING_ID)
        );

        // -3：当前终态不允许 release
        assertThat(releaseResult).isEqualTo(-3L);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("CONFIRMED");
    }

    @DisplayName("Reservation Redis Service 应返回明确的 reserve 业务结果")
    @Test
    void reservationServiceShouldReturnExplicitReserveResult() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        ReservationReserveOutcome first =
                reservationRedisService.reserve(RESERVATION_ID, SCREENING_ID, 1);

        ReservationReserveOutcome second =
                reservationRedisService.reserve(RESERVATION_ID, SCREENING_ID, 1);

        assertThat(first.result()).isEqualTo(ReservationReserveResult.RESERVED);
        assertThat(first.leftStock()).isEqualTo(9L);
        assertThat(second.result()).isEqualTo(ReservationReserveResult.IDEMPOTENT);

        // 幂等重放不伪造"第一次成功时的剩余库存"
        assertThat(second.leftStock()).isNull();

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");
    }

    @DisplayName("reserve 成功时应一次返回业务结果和扣减后的剩余库存")
    @Test
    void reservationServiceShouldReturnLeftStockWhenReserved() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        ReservationReserveOutcome outcome =
                reservationRedisService.reserve(RESERVATION_ID, SCREENING_ID, 1);

        assertThat(outcome.result()).isEqualTo(ReservationReserveResult.RESERVED);
        assertThat(outcome.leftStock()).isEqualTo(9L);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");
    }

    @DisplayName("Reservation Service release 应正确转换 Lua 返回结果")
    @Test
    void reservationServiceShouldMapReleaseResult() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        ReservationReserveOutcome reserveOutcome =
                reservationRedisService.reserve(RESERVATION_ID, SCREENING_ID, 1);

        assertThat(reserveOutcome.result()).isEqualTo(ReservationReserveResult.RESERVED);
        assertThat(reserveOutcome.leftStock()).isEqualTo(9L);

        ReservationTransitionResult first =
                reservationRedisService.release(RESERVATION_ID, SCREENING_ID);

        ReservationTransitionResult second =
                reservationRedisService.release(RESERVATION_ID, SCREENING_ID);

        assertThat(first).isEqualTo(ReservationTransitionResult.APPLIED);
        assertThat(second).isEqualTo(ReservationTransitionResult.IDEMPOTENT);

        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("10");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("RELEASED");
    }

    @DisplayName("Reservation Service compensate 应正确转换 Lua 返回结果")
    @Test
    void reservationServiceShouldMapCompensateResult() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        ReservationReserveOutcome reserveOutcome =
                reservationRedisService.reserve(RESERVATION_ID, SCREENING_ID, 1);

        assertThat(reserveOutcome.result()).isEqualTo(ReservationReserveResult.RESERVED);
        assertThat(reserveOutcome.leftStock()).isEqualTo(9L);

        ReservationTransitionResult first =
                reservationRedisService.compensate(RESERVATION_ID, SCREENING_ID);

        ReservationTransitionResult second =
                reservationRedisService.compensate(RESERVATION_ID, SCREENING_ID);

        assertThat(first).isEqualTo(ReservationTransitionResult.APPLIED);
        assertThat(second).isEqualTo(ReservationTransitionResult.IDEMPOTENT);

        // compensate 两次，库存也只能恢复一次
        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("10");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("COMPENSATED");
    }

    @DisplayName("Reservation Service confirm 应正确转换 Lua 返回结果")
    @Test
    void reservationServiceShouldMapConfirmResult() {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(SCREENING_ID);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(RESERVATION_ID);

        redisTemplate.opsForValue().set(stockKey, "10");

        ReservationReserveOutcome reserveOutcome =
                reservationRedisService.reserve(RESERVATION_ID, SCREENING_ID, 1);

        assertThat(reserveOutcome.result()).isEqualTo(ReservationReserveResult.RESERVED);
        assertThat(reserveOutcome.leftStock()).isEqualTo(9L);

        ReservationTransitionResult first =
                reservationRedisService.confirm(RESERVATION_ID);

        ReservationTransitionResult second =
                reservationRedisService.confirm(RESERVATION_ID);

        assertThat(first).isEqualTo(ReservationTransitionResult.APPLIED);
        assertThat(second).isEqualTo(ReservationTransitionResult.IDEMPOTENT);

        // confirm 不恢复，也不再次扣库存
        assertThat(redisTemplate.opsForValue().get(stockKey)).isEqualTo("9");
        assertThat(redisTemplate.opsForHash().get(reservationKey, "status")).isEqualTo("CONFIRMED");
    }

    @DisplayName("释放 Reservation 时 screeningId 不匹配不得污染其他场次库存")
    @Test
    void releaseShouldRejectMismatchedScreeningId() {

        Long reservationScreeningId = 2001L;
        Long wrongScreeningId = 3001L;
        String reservationId = "reservation-screening-binding-test";

        String reservationStockKey = RedisKeyConstants.buildScreeningStockKey(reservationScreeningId);
        String wrongStockKey = RedisKeyConstants.buildScreeningStockKey(wrongScreeningId);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(reservationId);

        redisTemplate.opsForValue().set(reservationStockKey, "9");
        redisTemplate.opsForValue().set(wrongStockKey, "20");

        redisTemplate.opsForHash().put(reservationKey, "status", ReservationStatus.PRE_RESERVED.name());
        redisTemplate.opsForHash().put(reservationKey, "screeningId", String.valueOf(reservationScreeningId));
        redisTemplate.opsForHash().put(reservationKey, "quantity", "1");

        ReservationTransitionResult result =
                reservationRedisService.compensate(reservationId, wrongScreeningId);

        assertThat(result).isEqualTo(ReservationTransitionResult.SCREENING_MISMATCH);

        assertThat(redisTemplate.opsForValue().get(reservationStockKey)).isEqualTo("9");

        assertThat(redisTemplate.opsForValue().get(wrongStockKey)).isEqualTo("20");

        assertThat(redisTemplate.opsForHash().get(reservationKey, "status"))
                .isEqualTo(ReservationStatus.PRE_RESERVED.name());
    }

    /**
     * 准备有效销售窗口。
     *
     * @param screeningId 场次 ID
     * @param saleEndEpoch 销售截止时间 epoch 秒
     */
    private static void prepareSaleWindow(Long screeningId, long saleEndEpoch) {

        long nowEpoch = Instant.now().getEpochSecond();

        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(screeningId);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(screeningId);

        // saleStart = 当前时间前 60 秒，当前必然已开售
        redisTemplate.opsForValue().set(readyKey, String.valueOf(nowEpoch - 60), Duration.ofMinutes(2));

        // saleEnd = 当前时间后 60 秒，当前必然未停售
        redisTemplate.opsForValue().set(saleEndKey, String.valueOf(saleEndEpoch), Duration.ofMinutes(2));
    }

    /**
     * 读取 Redis Lua multi-bulk 返回值中的整数。
     */
    private static long scriptLong(List<?> result, int index) {

        assertThat(result).as("Redis Lua result").isNotNull();
        assertThat(result.size()).as("Redis Lua result size").isGreaterThan(index);

        Object value = result.get(index);

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof byte[] bytes) {
            return Long.parseLong(new String(bytes, StandardCharsets.UTF_8));
        }

        return Long.parseLong(String.valueOf(value));
    }
}
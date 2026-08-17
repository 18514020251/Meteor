package com.meteor.ticketing.service.idempotency;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.id.utils.SnowflakeIdGenerator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 *  Redis 集成测试
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-14
 */
class GrabRequestIdResolverRedisIntegrationTest {

    private static final Long USER_ID = 1001L;
    private static final Long SCREENING_ID = 2001L;
    private static final Long OTHER_SCREENING_ID = 2002L;

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;

    private GrabRequestIdResolver resolver;
    private String clientRequestId;

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
        clientRequestId = "it-" + UUID.randomUUID();

        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

        resolver = new GrabRequestIdResolver(redisTemplate, idGenerator);

        long saleEndEpoch = Instant.now().getEpochSecond() + 60;

        prepareSaleWindow(SCREENING_ID, saleEndEpoch);
        prepareSaleWindow(OTHER_SCREENING_ID, saleEndEpoch);
    }

    @AfterEach
    void cleanRedis() {
        redisTemplate.delete(
                List.of(
                        RedisKeyConstants.buildGrabRequestKey(USER_ID, clientRequestId),
                        RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID),
                        RedisKeyConstants.buildScreeningStockReadyKey(OTHER_SCREENING_ID),
                        RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID),
                        RedisKeyConstants.buildScreeningSaleEndKey(OTHER_SCREENING_ID)
                )
        );
    }

    @AfterAll
    static void closeRedis() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @DisplayName("相同客户端请求重复调用时应持久化并返回同一个 requestId")
    @Test
    void sameRequestShouldPersistAndReturnSameRequestId() {
        String first = resolver.resolve(
                USER_ID,
                SCREENING_ID,
                clientRequestId,
                1
        );

        String second = resolver.resolve(
                USER_ID,
                SCREENING_ID,
                clientRequestId,
                1
        );

        /*
         * 两次进入 Resolver 时，
         * Snowflake 本来可以产生两个不同 candidate。
         *
         * 但最终业务身份必须稳定。
         */
        assertThat(second).isEqualTo(first);

        String requestKey = RedisKeyConstants.buildGrabRequestKey(USER_ID, clientRequestId);

        Map<Object, Object> stored = redisTemplate.opsForHash().entries(requestKey);

        String expectedFingerprint = "screeningId=" + SCREENING_ID + "&quantity=1";
        assertThat(stored)
                .containsEntry("requestId", first)
                .containsEntry("fingerprint", expectedFingerprint);

        Long ttlMillis = redisTemplate.getExpire(requestKey, TimeUnit.MILLISECONDS);

        assertThat(ttlMillis).isNotNull().isPositive();
    }

    @DisplayName("相同 clientRequestId 修改业务参数时应拒绝且不得覆盖原身份")
    @Test
    void sameClientRequestIdWithDifferentFingerprintShouldConflict() {
        String first = resolver.resolve(
                USER_ID,
                SCREENING_ID,
                clientRequestId,
                1
        );

        BizException exception = catchThrowableOfType(() ->
                        resolver.resolve(
                                USER_ID,
                                OTHER_SCREENING_ID,
                                clientRequestId,
                                1
                        ),
                        BizException.class
        );

        assertThat(exception).isNotNull();

        assertThat(exception.getCode()).isEqualTo(CommonErrorCode.PARAM_ERROR.getCode());

        String requestKey = RedisKeyConstants.buildGrabRequestKey(USER_ID, clientRequestId);

        Map<Object, Object> stored = redisTemplate.opsForHash().entries(requestKey);

        /*
         * 冲突请求不能把第一次请求污染掉。
         */
        String expectedFingerprint = "screeningId=" + SCREENING_ID + "&quantity=1";
        assertThat(stored)
                .containsEntry("requestId", first)
                .containsEntry("fingerprint", expectedFingerprint);
    }

    @DisplayName("并发重复请求只能产生一个稳定 requestId")
    @Test
    void concurrentSameRequestShouldResolveToSingleRequestId() throws Exception {

        int concurrency = 50;

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);

        CountDownLatch startGate = new CountDownLatch(1);

        try {
            List<Future<String>> futures = java.util.stream.IntStream
                            .range(0, concurrency)
                            .mapToObj(i ->
                                    executor.submit(() -> {
                                        startGate.await();

                                        return resolver.resolve(
                                                USER_ID,
                                                SCREENING_ID,
                                                clientRequestId,
                                                1
                                        );
                                    })
                            )
                            .toList();


            startGate.countDown();

            Set<String> requestIds = new HashSet<>();

            for (Future<String> future : futures) {
                requestIds.add(
                        future.get(10, TimeUnit.SECONDS)
                );
            }


            assertThat(requestIds).hasSize(1);

            String stableRequestId = requestIds.iterator().next();

            String requestKey = RedisKeyConstants.buildGrabRequestKey(USER_ID, clientRequestId);

            Map<Object, Object> stored = redisTemplate.opsForHash().entries(requestKey);

            String expectedFingerprint = "screeningId=" + SCREENING_ID + "&quantity=1";
            assertThat(stored)
                    .containsEntry("requestId", stableRequestId)
                    .containsEntry("fingerprint", expectedFingerprint);

        } finally {
            executor.shutdownNow();
        }
    }


    @DisplayName("停售后新的 clientRequestId 不得创建 requestId")
    @Test
    void newRequestAfterSaleEndShouldBeRejectedAndNotPersisted() {

        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);

        redisTemplate.opsForValue().set(
                readyKey,
                String.valueOf(Instant.now().getEpochSecond() - 60),
                Duration.ofMinutes(2)
        );


        String saleEndKey =RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);

        redisTemplate.opsForValue().set(
                saleEndKey,
                String.valueOf(Instant.now().getEpochSecond() - 10),
                Duration.ofMinutes(2)
        );

        BizException exception =
                catchThrowableOfType(
                        () -> resolver.resolve(
                                USER_ID,
                                SCREENING_ID,
                                clientRequestId,
                                1
                        ),
                        BizException.class
                );

        /*
         * 期望：
         * saleEnd 已经过了，
         * 所以新的 clientRequestId 必须被拒绝。
         */
        assertThat(exception).isNotNull();

        String requestKey = RedisKeyConstants.buildGrabRequestKey(USER_ID, clientRequestId);

        /*
         * 更重要的是：
         * 失败以后 Redis 里不能偷偷创建 request identity。
         */
        assertThat(redisTemplate.hasKey(requestKey)).isFalse();
    }

    @DisplayName("停售前已存在的请求在停售后重试仍应返回原 requestId")
    @Test
    void existingRequestAfterSaleEndShouldReturnOriginalRequestId() {

        prepareSaleWindow(SCREENING_ID, Instant.now().getEpochSecond() + 60);

        String first = resolver.resolve(
                USER_ID,
                SCREENING_ID,
                clientRequestId,
                1
        );

        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);

        redisTemplate.opsForValue().set(
                saleEndKey,
                String.valueOf(Instant.now().getEpochSecond() - 10),
                Duration.ofMinutes(2)
        );

        String second = resolver.resolve(
                USER_ID,
                SCREENING_ID,
                clientRequestId,
                1
        );

        assertThat(second).isEqualTo(first);

        String requestKey = RedisKeyConstants.buildGrabRequestKey(USER_ID, clientRequestId);

        Map<Object, Object> stored = redisTemplate.opsForHash().entries(requestKey);

        assertThat(stored).containsEntry("requestId", first);

        String expectedFingerprint = "screeningId=" + SCREENING_ID + "&quantity=1";

        assertThat(stored).containsEntry("fingerprint", expectedFingerprint);
    }

    /**
     *  准备销售窗口
     *  @param screeningId 屏幕 ID
     *  @param saleEndEpoch 销售截止时间戳
     * */
    private static void prepareSaleWindow(Long screeningId, long saleEndEpoch) {
        long nowEpoch = Instant.now().getEpochSecond();

        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(screeningId);

        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(screeningId);

        redisTemplate.opsForValue().set(
                readyKey,
                String.valueOf(nowEpoch - 60),
                Duration.ofMinutes(2)
        );

        // 模拟销售截止时间
        redisTemplate.opsForValue().set(
                saleEndKey,
                String.valueOf(saleEndEpoch),
                Duration.ofMinutes(2)
        );
    }

    @DisplayName("readyKey 缺失时新的请求不得创建 requestId")
    @Test
    void newRequestWithoutReadyKeyShouldBeRejectedAndNotPersisted() {
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(SCREENING_ID);
        redisTemplate.delete(readyKey);

        BizException exception = catchThrowableOfType(
                () -> resolver.resolve(USER_ID, SCREENING_ID, clientRequestId, 1),
                BizException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(CommonErrorCode.BIZ_ERROR.getCode());

        String requestKey = RedisKeyConstants.buildGrabRequestKey(USER_ID, clientRequestId);
        assertThat(redisTemplate.hasKey(requestKey)).isFalse();
    }

    @DisplayName("saleEndKey 缺失时新的请求不得创建 requestId")
    @Test
    void newRequestWithoutSaleEndKeyShouldBeRejectedAndNotPersisted() {

        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(SCREENING_ID);

        redisTemplate.delete(saleEndKey);

        BizException exception =
                catchThrowableOfType(() ->
                                resolver.resolve(
                                USER_ID,
                                SCREENING_ID,
                                clientRequestId,
                                1
                        ),
                        BizException.class
                );

        assertThat(exception).isNotNull();

        String requestKey = RedisKeyConstants.buildGrabRequestKey(USER_ID, clientRequestId);

        assertThat(redisTemplate.hasKey(requestKey)).isFalse();
    }
}

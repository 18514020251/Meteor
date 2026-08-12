package com.meteor.ticketing.mapper;

import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-12
 */
@SpringBootTest(
        classes = ScreeningMapperIntegrationTest.TestApplication.class,
        properties = "spring.main.web-application-type=none"
)
@ActiveProfiles("test")
class ScreeningMapperIntegrationTest {

    private static final Long SCREENING_ID = 990001L;

    @Autowired
    private ScreeningMapper screeningMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.update(
                "DELETE FROM screening WHERE id = ?",
                SCREENING_ID
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableMeteorMyBatisPlus
    @MapperScan("com.meteor.ticketing.mapper")
    static class TestApplication {
    }

    /**
     * @param available 可售库存
     * @param sold 已售库存
     * */
    private void insertStock(
            int available,
            int sold
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO screening
                (
                    id,
                    available_tickets,
                    sold_tickets,
                    version,
                    update_time,
                    deleted
                )
                VALUES (?, ?, ?, 0, NOW(), 0)
                """,
                SCREENING_ID,
                available,
                sold
        );
    }

    /**
     * @return 可售库存
     * */
    private Integer queryAvailable() {
        return jdbcTemplate.queryForObject(
                """
                SELECT available_tickets
                FROM screening
                WHERE id = ?
                """,
                Integer.class,
                SCREENING_ID
        );
    }

    /**
     * @return 已售库存
     * */
    private Integer querySold() {
        return jdbcTemplate.queryForObject(
                """
                SELECT sold_tickets
                FROM screening
                WHERE id = ?
                """,
                Integer.class,
                SCREENING_ID
        );
    }

    @DisplayName("可售库存为0时，扣减SQL必须拒绝更新")
    @Test
    void shouldRejectDecreaseWhenAvailableStockIsZero() {
        // Arrange
        insertStock(0, 10);

        // Act
        int affectedRows =
                screeningMapper.decreaseAvailableAndIncreaseSold(
                        SCREENING_ID,
                        1,
                        LocalDateTime.now()
                );

        // Assert
        assertThat(affectedRows).isZero();

        assertThat(queryAvailable()).isZero();
        assertThat(querySold()).isEqualTo(10);
    }

    @DisplayName("已售库存为0时，释放SQL必须拒绝更新")
    @Test
    void shouldRejectReleaseWhenSoldStockIsZero() {
        // Arrange
        insertStock(10, 0);

        // Act
        int affectedRows =
                screeningMapper.increaseAvailableAndDecreaseSold(
                        SCREENING_ID,
                        1,
                        LocalDateTime.now()
                );

        // Assert
        assertThat(affectedRows).isZero();

        assertThat(queryAvailable()).isEqualTo(10);
        assertThat(querySold()).isZero();
    }

    @DisplayName("库存充足时，扣减应同时减少可售并增加已售")
    @Test
    void shouldDecreaseAvailableAndIncreaseSold() {
        // Arrange
        insertStock(1, 9);

        // Act
        int affectedRows =
                screeningMapper.decreaseAvailableAndIncreaseSold(
                        SCREENING_ID,
                        1,
                        LocalDateTime.now()
                );

        // Assert
        assertThat(affectedRows).isEqualTo(1);

        int available = queryAvailable();
        int sold = querySold();

        assertThat(available).isZero();
        assertThat(sold).isEqualTo(10);

        assertThat(available + sold).isEqualTo(10);
    }

    @DisplayName("已售库存充足时，释放应同时增加可售并减少已售")
    @Test
    void shouldIncreaseAvailableAndDecreaseSold() {
        // Arrange
        insertStock(8, 2);

        // Act
        int affectedRows =
                screeningMapper.increaseAvailableAndDecreaseSold(
                        SCREENING_ID,
                        2,
                        LocalDateTime.now()
                );

        // Assert
        assertThat(affectedRows).isEqualTo(1);

        int available = queryAvailable();
        int sold = querySold();

        assertThat(available).isEqualTo(10);
        assertThat(sold).isZero();

        assertThat(available + sold).isEqualTo(10);
    }

    @DisplayName("两个线程同时扣最后一张票时，只允许一个成功")
    @Test
    void concurrentDecreaseShouldAllowOnlyOneSuccess()
            throws Exception {

        insertStock(1, 9);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {
            CountDownLatch ready =
                    new CountDownLatch(2);

            CountDownLatch start =
                    new CountDownLatch(1);

            Callable<Integer> task = () -> {

                ready.countDown();

                start.await();

                return screeningMapper
                        .decreaseAvailableAndIncreaseSold(
                                SCREENING_ID,
                                1,
                                LocalDateTime.now()
                        );
            };

            Future<Integer> first =
                    executor.submit(task);

            Future<Integer> second =
                    executor.submit(task);

            boolean allReady =
                    ready.await(3, TimeUnit.SECONDS);

            assertThat(allReady).isTrue();

            start.countDown();

            int firstRows = first.get(3, TimeUnit.SECONDS);

            int secondRows = second.get(3, TimeUnit.SECONDS);

            assertThat(firstRows + secondRows)
                    .isEqualTo(1);

            int available = queryAvailable();
            int sold = querySold();

            assertThat(available).isZero();
            assertThat(sold).isEqualTo(10);

            assertThat(available + sold)
                    .isEqualTo(10);

        } finally {
            executor.shutdownNow();
        }
    }
    @DisplayName("两个线程同时释放最后一个 sold")
    @Test
    void concurrentReleaseShouldAllowOnlyOneSuccess()
            throws Exception {
        insertStock(9, 1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {
            CountDownLatch ready =
                    new CountDownLatch(2);

            CountDownLatch start =
                    new CountDownLatch(1);

            Callable<Integer> task = () -> {

                ready.countDown();

                start.await();

                return screeningMapper
                        .increaseAvailableAndDecreaseSold(
                                SCREENING_ID,
                                1,
                                LocalDateTime.now()
                        );
            };

            Future<Integer> first =
                    executor.submit(task);

            Future<Integer> second =
                    executor.submit(task);

            boolean allReady =
                    ready.await(3, TimeUnit.SECONDS);

            assertThat(allReady).isTrue();

            start.countDown();

            int firstRows = first.get(3, TimeUnit.SECONDS);

            int secondRows = second.get(3, TimeUnit.SECONDS);

            assertThat(firstRows + secondRows)
                    .isEqualTo(1);

            int available = queryAvailable();
            int sold = querySold();

            assertThat(sold).isZero();
            assertThat(available).isEqualTo(10);

            assertThat(available + sold)
                    .isEqualTo(10);

        } finally {
            executor.shutdownNow();
        }
    }

    @DisplayName("同一库存连续释放两次时，第二次应被数据库拒绝")
    @Test
    void secondReleaseShouldFailWhenSoldStockAlreadyReleased() {
        // Arrange
        insertStock(9, 1);

        // Act
        int firstRows =
                screeningMapper.increaseAvailableAndDecreaseSold(
                        SCREENING_ID,
                        1,
                        LocalDateTime.now()
                );

        int secondRows =
                screeningMapper.increaseAvailableAndDecreaseSold(
                        SCREENING_ID,
                        1,
                        LocalDateTime.now()
                );

        // Assert
        assertThat(firstRows).isEqualTo(1);
        assertThat(secondRows).isZero();

        assertThat(queryAvailable()).isEqualTo(10);
        assertThat(querySold()).isZero();
    }



}

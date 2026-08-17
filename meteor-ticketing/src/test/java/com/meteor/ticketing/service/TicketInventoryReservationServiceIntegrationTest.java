package com.meteor.ticketing.service;

import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import com.meteor.ticketing.domain.entity.TicketInventoryReservation;
import com.meteor.ticketing.enums.ReservationPersistResult;
import com.meteor.ticketing.enums.ReservationStatus;
import com.meteor.ticketing.mapper.TicketInventoryReservationMapper;
import com.meteor.ticketing.service.impl.TicketInventoryReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 *  税票库存预约服务集成测试
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-17
 */
@SpringBootTest(
        classes = TicketInventoryReservationServiceIntegrationTest.TestApplication.class,
        properties = "spring.main.web-application-type=none"
)
@ActiveProfiles("test")
class TicketInventoryReservationServiceIntegrationTest {

    private static final String RESERVATION_ID =
            "reservation-m1b06-service-001";

    private static final String CLIENT_REQUEST_ID =
            "client-m1b06-service-001";

    private static final Long SCREENING_ID = 990001L;

    private static final Long USER_ID = 10001L;

    @Autowired
    private ITicketInventoryReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TicketInventoryReservationMapper reservationMapper;

    @BeforeEach
    void clean() {
        jdbcTemplate.update(
                """
                DELETE FROM ticket_inventory_reservation
                WHERE reservation_id LIKE 'reservation-m1b06-%'
                   OR client_request_id LIKE 'client-m1b06-%'
                """
        );
    }

    @DisplayName("相同 Reservation 重复持久化时应识别为幂等且数据库只保留一条")
    @Test
    void sameReservationShouldBeIdempotent() {

        TicketInventoryReservation first = buildReservation();

        ReservationPersistResult firstResult = reservationService.persistPreReserved(first);

        ReservationPersistResult secondResult =
                reservationService.persistPreReserved(buildReservation());

        assertThat(firstResult).isEqualTo(ReservationPersistResult.CREATED);

        assertThat(secondResult).isEqualTo(ReservationPersistResult.IDEMPOTENT);

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM ticket_inventory_reservation
                WHERE reservation_id = ?
                """,
                Integer.class,
                RESERVATION_ID
        );

        assertThat(count).isEqualTo(1);
    }

    @DisplayName("同一用户和 clientRequestId 被不同 reservationId 占用时应识别为冲突")
    @Test
    void sameUserAndClientRequestIdWithDifferentReservationIdShouldConflict() {

        String firstReservationId =
                "reservation-m1b06-conflict-001";

        String secondReservationId =
                "reservation-m1b06-conflict-002";

        String clientRequestId =
                "client-m1b06-conflict-001";

        Long userId = 10002L;
        Long screeningId = 990002L;

        TicketInventoryReservation first =
                new TicketInventoryReservation()
                        .setReservationId(firstReservationId)
                        .setClientRequestId(clientRequestId)
                        .setScreeningId(screeningId)
                        .setUserId(userId)
                        .setQuantity(1)
                        .setStatus(ReservationStatus.PRE_RESERVED);

        TicketInventoryReservation second =
                new TicketInventoryReservation()
                        .setReservationId(secondReservationId)
                        .setClientRequestId(clientRequestId)
                        .setScreeningId(screeningId)
                        .setUserId(userId)
                        .setQuantity(1)
                        .setStatus(ReservationStatus.PRE_RESERVED);

        ReservationPersistResult firstResult =
                reservationService.persistPreReserved(first);

        ReservationPersistResult secondResult =
                reservationService.persistPreReserved(second);

        assertThat(firstResult)
                .isEqualTo(ReservationPersistResult.CREATED);

        assertThat(secondResult)
                .isEqualTo(ReservationPersistResult.CONFLICT);

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM ticket_inventory_reservation
                WHERE user_id = ?
                  AND client_request_id = ?
                """,
                Integer.class,
                userId,
                clientRequestId
        );

        assertThat(count)
                .isEqualTo(1);

        Integer firstCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM ticket_inventory_reservation
                WHERE reservation_id = ?
                """,
                Integer.class,
                firstReservationId
        );

        Integer secondCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM ticket_inventory_reservation
                WHERE reservation_id = ?
                """,
                Integer.class,
                secondReservationId
        );

        assertThat(firstCount)
                .isEqualTo(1);

        assertThat(secondCount)
                .isZero();
    }

    @DisplayName("相同 reservationId 但不可变业务身份不一致时应识别为冲突")
    @Test
    void sameReservationIdWithDifferentImmutableIdentityShouldConflict() {

        String reservationId =
                "reservation-m1b06-identity-conflict-001";

        String clientRequestId =
                "client-m1b06-identity-conflict-001";

        Long userId = 10003L;

        TicketInventoryReservation first =
                new TicketInventoryReservation()
                        .setReservationId(reservationId)
                        .setClientRequestId(clientRequestId)
                        .setScreeningId(990003L)
                        .setUserId(userId)
                        .setQuantity(1)
                        .setStatus(ReservationStatus.PRE_RESERVED);

        TicketInventoryReservation second =
                new TicketInventoryReservation()
                        .setReservationId(reservationId)
                        .setClientRequestId(clientRequestId)
                        .setScreeningId(990004L)
                        .setUserId(userId)
                        .setQuantity(1)
                        .setStatus(ReservationStatus.PRE_RESERVED);

        ReservationPersistResult firstResult =
                reservationService.persistPreReserved(first);

        ReservationPersistResult secondResult =
                reservationService.persistPreReserved(second);

        assertThat(firstResult)
                .isEqualTo(ReservationPersistResult.CREATED);

        assertThat(secondResult)
                .isEqualTo(ReservationPersistResult.CONFLICT);

        TicketInventoryReservation persisted =
                reservationMapper.selectById(reservationId);

        assertThat(persisted)
                .isNotNull();

        assertThat(persisted.getScreeningId())
                .isEqualTo(990003L);
    }

    private TicketInventoryReservation buildReservation() {
        return new TicketInventoryReservation()
                .setReservationId(RESERVATION_ID)
                .setClientRequestId(CLIENT_REQUEST_ID)
                .setScreeningId(SCREENING_ID)
                .setUserId(USER_ID)
                .setQuantity(1)
                .setStatus(ReservationStatus.PRE_RESERVED);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableMeteorMyBatisPlus
    @MapperScan("com.meteor.ticketing.mapper")
    @Import(TicketInventoryReservationServiceImpl.class)
    static class TestApplication {
    }
}

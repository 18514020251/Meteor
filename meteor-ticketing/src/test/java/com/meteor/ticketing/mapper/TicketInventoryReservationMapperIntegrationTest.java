package com.meteor.ticketing.mapper;

import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import com.meteor.ticketing.domain.entity.TicketInventoryReservation;
import com.meteor.ticketing.enums.ReservationStatus;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 *  票务库存预留表
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-17
 */
@SpringBootTest(
        classes = TicketInventoryReservationMapperIntegrationTest.TestApplication.class,
        properties = "spring.main.web-application-type=none"
)
@ActiveProfiles("test")
class TicketInventoryReservationMapperIntegrationTest {

    private static final String RESERVATION_ID = "reservation-m1b06-mapper-001";
    private static final String CLIENT_REQUEST_ID = "client-m1b06-mapper-001";
    private static final Long SCREENING_ID = 990001L;
    private static final Long USER_ID = 10001L;

    @Autowired
    private TicketInventoryReservationMapper reservationMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.update(
                """
                DELETE FROM ticket_inventory_reservation
                WHERE reservation_id = ?
                   OR (user_id = ? AND client_request_id = ?)
                """,
                RESERVATION_ID,
                USER_ID,
                CLIENT_REQUEST_ID
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableMeteorMyBatisPlus
    @MapperScan("com.meteor.ticketing.mapper")
    static class TestApplication {
    }

    @DisplayName("库存预留写入后应能根据 reservationId 完整还原业务身份")
    @Test
    void shouldPersistAndRestoreReservationByReservationId() {
        // Arrange
        TicketInventoryReservation reservation =
                new TicketInventoryReservation()
                        .setReservationId(RESERVATION_ID)
                        .setClientRequestId(CLIENT_REQUEST_ID)
                        .setScreeningId(SCREENING_ID)
                        .setUserId(USER_ID)
                        .setQuantity(1)
                        .setStatus(ReservationStatus.PRE_RESERVED);

        // Act
        int affectedRows = reservationMapper.insert(reservation);
        TicketInventoryReservation persisted =
                reservationMapper.selectById(RESERVATION_ID);

        // Assert
        assertThat(affectedRows).isEqualTo(1);
        assertThat(persisted).isNotNull();
        assertThat(persisted.getReservationId()).isEqualTo(RESERVATION_ID);
        assertThat(persisted.getClientRequestId()).isEqualTo(CLIENT_REQUEST_ID);
        assertThat(persisted.getScreeningId()).isEqualTo(SCREENING_ID);
        assertThat(persisted.getUserId()).isEqualTo(USER_ID);
        assertThat(persisted.getQuantity()).isEqualTo(1);
        assertThat(persisted.getStatus()).isEqualTo(ReservationStatus.PRE_RESERVED);
        assertThat(persisted.getExpireAt()).isNull();
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getUpdatedAt()).isNotNull();
    }
}

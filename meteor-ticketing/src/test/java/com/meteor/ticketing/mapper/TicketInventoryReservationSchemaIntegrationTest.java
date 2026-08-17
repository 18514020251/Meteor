package com.meteor.ticketing.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 *  测试票库存预约表的数据库 schema
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-17
 */
@SpringBootTest(
        classes = TicketInventoryReservationSchemaIntegrationTest.TestApplication.class,
        properties = "spring.main.web-application-type=none"
)
@ActiveProfiles("test")
class TicketInventoryReservationSchemaIntegrationTest {

    private static final String RESERVATION_ID = "reservation-m1b06-001";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }

    @DisplayName("同一 reservationId 重复插入时数据库必须拒绝第二条记录")
    @Test
    void duplicateReservationIdShouldBeRejected() {
        jdbcTemplate.update(
                "DELETE FROM ticket_inventory_reservation WHERE reservation_id = ?",
                RESERVATION_ID
        );

        insertReservation();

        assertThatThrownBy(this::insertReservation)
                .isInstanceOf(Exception.class);
    }

    private void insertReservation() {

        jdbcTemplate.update(
                """
                INSERT INTO ticket_inventory_reservation
                (
                    reservation_id,
                    client_request_id,
                    screening_id,
                    user_id,
                    quantity,
                    status,
                    expire_at,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL 3 MINUTE), NOW(), NOW())
                """,
                RESERVATION_ID,
                "client-request-m1b06-001",
                990001L,
                10001L,
                1,
                "PRE_RESERVED"
        );
    }
}

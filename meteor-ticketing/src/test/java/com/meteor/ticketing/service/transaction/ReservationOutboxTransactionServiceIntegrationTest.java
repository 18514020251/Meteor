package com.meteor.ticketing.service.transaction;

import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.domain.entity.TicketInventoryReservation;
import com.meteor.ticketing.enums.OutboxStatus;
import com.meteor.ticketing.enums.ReservationStatus;
import com.meteor.ticketing.service.impl.MqOutboxEventServiceImpl;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *  订单创建时 Reservation 和 Outbox 必须同时提交或回滚
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-17
 */
@SpringBootTest(
        classes = ReservationOutboxTransactionServiceIntegrationTest.TestApplication.class,
        properties = "spring.main.web-application-type=none"
)
@ActiveProfiles("test")
class ReservationOutboxTransactionServiceIntegrationTest {

    private static final String RESERVATION_ID =
            "reservation-m1b07-rollback-001";

    private static final String CLIENT_REQUEST_ID =
            "client-m1b07-rollback-001";

    private static final String BIZ_KEY =
            "order-m1b07-rollback-001";

    private static final String EVENT_TYPE =
            "TICKET_ORDER_CREATE";

    @Autowired
    private ReservationOutboxTransactionService transactionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {

        jdbcTemplate.update(
                """
                DELETE FROM ticket_inventory_reservation
                WHERE reservation_id LIKE 'reservation-m1b07-%'
                   OR client_request_id LIKE 'client-m1b07-%'
                """
        );

        jdbcTemplate.update(
                """
                DELETE FROM mq_outbox_event
                WHERE biz_key LIKE 'order-m1b07-%'
                """
        );
    }

    @DisplayName("Outbox 写入失败时 Reservation 必须随事务一起回滚")
    @Test
    void reservationShouldRollbackWhenOutboxInsertFails() {

        insertExistingOutbox();

        TicketInventoryReservation reservation = buildReservation();

        MqOutboxEvent duplicateOutbox = buildOutbox(900000000002L);

        assertThatThrownBy(
                () -> transactionService.persist(
                        reservation,
                        duplicateOutbox
                )
        )
                .isInstanceOf(ReservationOutboxRollbackException.class)
                .hasCauseInstanceOf(DuplicateKeyException.class);

        Integer reservationCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ticket_inventory_reservation
                        WHERE reservation_id = ?
                        """,
                        Integer.class,
                        RESERVATION_ID
                );

        assertThat(reservationCount)
                .as("Outbox 失败后 Reservation 不允许残留")
                .isZero();

        Integer outboxCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM mq_outbox_event
                        WHERE event_type = ?
                          AND biz_key = ?
                        """,
                        Integer.class,
                        EVENT_TYPE,
                        BIZ_KEY
                );

        /*
         * 只允许测试预先插入的那一条存在。
         */
        assertThat(outboxCount)
                .isEqualTo(1);


    }

    private TicketInventoryReservation buildReservation() {
        return new TicketInventoryReservation()
                .setReservationId(RESERVATION_ID)
                .setClientRequestId(CLIENT_REQUEST_ID)
                .setScreeningId(990007L)
                .setUserId(10007L)
                .setQuantity(1)
                .setStatus(ReservationStatus.PRE_RESERVED);
    }

    private void insertExistingOutbox() {
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(
                """
                INSERT INTO mq_outbox_event
                (
                    id,
                    biz_key,
                    event_type,
                    exchange_name,
                    routing_key,
                    payload,
                    status,
                    retry_cnt,
                    next_retry_time,
                    deliver_at,
                    biz_expire_at,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                900000000001L,
                BIZ_KEY,
                EVENT_TYPE,
                "meteor.ticketing.exchange",
                "ticket.order.create",
                "{}",
                0,
                0,
                now,
                now,
                now.plusMinutes(3),
                now,
                now
        );
    }

    private MqOutboxEvent buildOutbox(Long id) {
        LocalDateTime now = LocalDateTime.now();

        return new MqOutboxEvent()
                .setId(id)
                .setBizKey(BIZ_KEY)
                .setEventType(EVENT_TYPE)
                .setExchangeName("meteor.ticketing.exchange")
                .setRoutingKey("ticket.order.create")
                .setPayload("{}")
                .setStatus(OutboxStatus.NEW)
                .setRetryCnt(0)
                .setNextRetryTime(now)
                .setDeliverAt(now)
                .setBizExpireAt(now.plusMinutes(3))
                .setCreatedAt(now)
                .setUpdatedAt(now);
    }

    @DisplayName("Reservation 和 Outbox 写入成功时必须同时提交")
    @Test
    void reservationAndOutboxShouldCommitTogether() {

        String reservationId =
                "reservation-m1b07-commit-001";

        String clientRequestId =
                "client-m1b07-commit-001";

        String bizKey =
                "order-m1b07-commit-001";

        TicketInventoryReservation reservation =
                new TicketInventoryReservation()
                        .setReservationId(reservationId)
                        .setClientRequestId(clientRequestId)
                        .setScreeningId(990008L)
                        .setUserId(10008L)
                        .setQuantity(1)
                        .setStatus(ReservationStatus.PRE_RESERVED);

        LocalDateTime now = LocalDateTime.now();

        MqOutboxEvent outbox =
                new MqOutboxEvent()
                        .setId(900000000003L)
                        .setBizKey(bizKey)
                        .setEventType(EVENT_TYPE)
                        .setExchangeName("meteor.ticketing.exchange")
                        .setRoutingKey("ticket.order.create")
                        .setPayload("{}")
                        .setStatus(OutboxStatus.NEW)
                        .setRetryCnt(0)
                        .setNextRetryTime(now)
                        .setDeliverAt(now)
                        .setBizExpireAt(now.plusMinutes(3))
                        .setCreatedAt(now)
                        .setUpdatedAt(now);

        transactionService.persist(
                reservation,
                outbox
        );

        Integer reservationCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ticket_inventory_reservation
                        WHERE reservation_id = ?
                        """,
                        Integer.class,
                        reservationId
                );

        Integer outboxCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM mq_outbox_event
                        WHERE event_type = ?
                          AND biz_key = ?
                        """,
                        Integer.class,
                        EVENT_TYPE,
                        bizKey
                );

        assertThat(reservationCount)
                .isEqualTo(1);

        assertThat(outboxCount)
                .isEqualTo(1);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableMeteorMyBatisPlus
    @MapperScan("com.meteor.ticketing.mapper")
    @Import({
            TicketInventoryReservationServiceImpl.class,
            MqOutboxEventServiceImpl.class,
            ReservationOutboxTransactionServiceImpl.class
    })
    static class TestApplication {
    }
}

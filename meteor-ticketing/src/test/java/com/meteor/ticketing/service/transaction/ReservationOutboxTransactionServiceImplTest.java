package com.meteor.ticketing.service.transaction;

import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.domain.entity.TicketInventoryReservation;
import com.meteor.ticketing.enums.ReservationPersistResult;
import com.meteor.ticketing.service.IMqOutboxEventService;
import com.meteor.ticketing.service.ITicketInventoryReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 *  预定消息补偿事务服务测试类
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-18
 */
@ExtendWith(MockitoExtension.class)
class ReservationOutboxTransactionServiceImplTest {

    @Mock
    private ITicketInventoryReservationService reservationService;

    @Mock
    private IMqOutboxEventService outboxService;

    private ReservationOutboxTransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new ReservationOutboxTransactionServiceImpl(
                reservationService,
                outboxService
        );
    }

    @DisplayName("Outbox save 返回 false 时必须抛出明确回滚异常")
    @Test
    void persistShouldThrowDefiniteRollbackExceptionWhenOutboxSaveReturnsFalse() {

        TicketInventoryReservation reservation = new TicketInventoryReservation();
        MqOutboxEvent outboxEvent = new MqOutboxEvent();

        when(reservationService.persistPreReserved(reservation))
                .thenReturn(ReservationPersistResult.CREATED);

        when(outboxService.save(outboxEvent))
                .thenReturn(false);

        assertThatThrownBy(() -> transactionService.persist(reservation, outboxEvent))
                .isInstanceOf(ReservationOutboxRollbackException.class)
                .hasMessageContaining("Outbox");
    }
}

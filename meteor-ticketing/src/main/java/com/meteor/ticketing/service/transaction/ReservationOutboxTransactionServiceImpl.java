package com.meteor.ticketing.service.transaction;

import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.domain.entity.TicketInventoryReservation;
import com.meteor.ticketing.enums.ReservationPersistResult;
import com.meteor.ticketing.service.IMqOutboxEventService;
import com.meteor.ticketing.service.ITicketInventoryReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *  保存订单库存扣除消息到数据库
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-17
 */
@Service
@RequiredArgsConstructor
public class ReservationOutboxTransactionServiceImpl
        implements ReservationOutboxTransactionService {

    private final ITicketInventoryReservationService reservationService;
    private final IMqOutboxEventService outboxService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persist(TicketInventoryReservation reservation, MqOutboxEvent outboxEvent) {
        try {
            ReservationPersistResult result = reservationService.persistPreReserved(reservation);

            if (result != ReservationPersistResult.CREATED) {
                throw new ReservationOutboxRollbackException(
                        "Reservation 创建结果异常: " + result
                );
            }

            boolean saved = outboxService.save(outboxEvent);

            if (!saved) {
                throw new ReservationOutboxRollbackException(
                        "Outbox 写入失败"
                );
            }

        } catch (ReservationOutboxRollbackException e) {
            throw e;

        } catch (Exception e) {
            /*
             * 这里只能捕获事务方法体执行期间发生的异常。
             *
             * 例如：
             * - Reservation INSERT 数据库异常
             * - Outbox INSERT 数据库异常
             * - 唯一键冲突
             *
             * 方法因此异常退出，Spring 会 rollback。
             *
             * commit 阶段异常发生在方法返回之后，
             * 不会被这个 catch 捕获，留给 M1B-07-4。
             */
            throw new ReservationOutboxRollbackException(
                    "Reservation + Outbox 本地事务执行失败",
                    e
            );
        }
    }
}

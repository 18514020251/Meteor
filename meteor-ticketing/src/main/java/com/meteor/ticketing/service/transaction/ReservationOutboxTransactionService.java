package com.meteor.ticketing.service.transaction;

import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.domain.entity.TicketInventoryReservation;

/**
 *  保存订单库存扣除消息到数据库
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-17
 */
public interface ReservationOutboxTransactionService {

    void persist(TicketInventoryReservation reservation, MqOutboxEvent outboxEvent);
}
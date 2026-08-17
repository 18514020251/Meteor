package com.meteor.ticketing.service;

import com.meteor.ticketing.domain.entity.TicketInventoryReservation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.ticketing.enums.ReservationPersistResult;

/**
 * <p>
 * 票务库存预留表 服务类
 * </p>
 *
 * @author 昭兮
 * @since 2026-08-17
 */
public interface ITicketInventoryReservationService extends IService<TicketInventoryReservation> {

    ReservationPersistResult persistPreReserved(TicketInventoryReservation reservation);
}

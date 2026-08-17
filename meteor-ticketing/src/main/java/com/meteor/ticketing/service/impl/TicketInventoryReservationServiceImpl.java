package com.meteor.ticketing.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.ticketing.domain.entity.TicketInventoryReservation;
import com.meteor.ticketing.enums.ReservationPersistResult;
import com.meteor.ticketing.mapper.TicketInventoryReservationMapper;
import com.meteor.ticketing.service.ITicketInventoryReservationService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 票务库存预留表 服务实现类
 * </p>
 *
 * @author 昭兮
 * @since 2026-08-17
 */
@Service
public class TicketInventoryReservationServiceImpl extends ServiceImpl<TicketInventoryReservationMapper, TicketInventoryReservation> implements ITicketInventoryReservationService {

    @Override
    public ReservationPersistResult persistPreReserved(TicketInventoryReservation reservation) {
        try {
            baseMapper.insert(reservation);
            return ReservationPersistResult.CREATED;

        } catch (DuplicateKeyException ex) {

            List<TicketInventoryReservation> candidates =
                    baseMapper.selectList(
                            Wrappers.<TicketInventoryReservation>lambdaQuery()
                                    .select(
                                            TicketInventoryReservation::getReservationId,
                                            TicketInventoryReservation::getClientRequestId,
                                            TicketInventoryReservation::getScreeningId,
                                            TicketInventoryReservation::getUserId,
                                            TicketInventoryReservation::getQuantity
                                    )
                                    .eq(
                                            TicketInventoryReservation::getReservationId,
                                            reservation.getReservationId()
                                    )
                                    .or(wrapper -> wrapper
                                            .eq(
                                                    TicketInventoryReservation::getUserId,
                                                    reservation.getUserId()
                                            )
                                            .eq(
                                                    TicketInventoryReservation::getClientRequestId,
                                                    reservation.getClientRequestId()
                                            )
                                    )
                    );

            if (candidates.isEmpty()) {
                throw ex;
            }

            for (TicketInventoryReservation existing : candidates) {
                if (Objects.equals(
                        existing.getReservationId(),
                        reservation.getReservationId()
                )) {
                    return sameImmutableIdentity(existing, reservation)
                            ? ReservationPersistResult.IDEMPOTENT
                            : ReservationPersistResult.CONFLICT;
                }
            }

            return ReservationPersistResult.CONFLICT;
        }
    }

    /**
     * 判断两条 Reservation 是否代表同一个不可变业务事实。
     *
     * status、expireAt、createdAt、updatedAt 都可能随着生命周期变化，
     * 因此不参与幂等身份判断。
     */
    private boolean sameImmutableIdentity(
            TicketInventoryReservation existing,
            TicketInventoryReservation incoming
    ) {
        return Objects.equals(existing.getReservationId(), incoming.getReservationId())
                && Objects.equals(existing.getClientRequestId(), incoming.getClientRequestId())
                && Objects.equals(existing.getScreeningId(), incoming.getScreeningId())
                && Objects.equals(existing.getUserId(), incoming.getUserId())
                && Objects.equals(existing.getQuantity(), incoming.getQuantity());
    }
}
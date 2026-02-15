package com.meteor.ticketing.service.impl;

import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.meteor.ticketing.enums.OutboxStatus;
import com.meteor.ticketing.mapper.MqOutboxEventMapper;
import com.meteor.ticketing.service.IMqOutboxEventService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-15
 */
@Service
@RequiredArgsConstructor
public class MqOutboxEventServiceImpl extends ServiceImpl<MqOutboxEventMapper, MqOutboxEvent> implements IMqOutboxEventService {

    @Override
    public List<MqOutboxEvent> listDueEvents(int limit) {
        LocalDateTime now = LocalDateTime.now();
        return lambdaQuery()
                .in(MqOutboxEvent::getStatus, OutboxStatus.NEW, OutboxStatus.FAIL)
                .le(MqOutboxEvent::getNextRetryTime, now)
                .le(MqOutboxEvent::getDeliverAt, now)
                .orderByAsc(MqOutboxEvent::getNextRetryTime)
                .orderByAsc(MqOutboxEvent::getId)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    public boolean markSent(Long id) {
        return lambdaUpdate()
                .set(MqOutboxEvent::getUpdatedAt, LocalDateTime.now())
                .set(MqOutboxEvent::getStatus, OutboxStatus.SENT)
                .eq(MqOutboxEvent::getId, id)
                .in(MqOutboxEvent::getStatus, OutboxStatus.NEW, OutboxStatus.FAIL)
                .update();
    }

    @Override
    public boolean markExpired(Long id) {
        return lambdaUpdate()
                .set(MqOutboxEvent::getStatus, OutboxStatus.EXPIRED)
                .set(MqOutboxEvent::getUpdatedAt, LocalDateTime.now())
                .eq(MqOutboxEvent::getId, id)
                .in(MqOutboxEvent::getStatus, OutboxStatus.NEW, OutboxStatus.FAIL)
                .update();
    }

    @Override
    public boolean markDead(Long id, String lastError) {
        return lambdaUpdate()
                .set(MqOutboxEvent::getStatus, OutboxStatus.DEAD)
                .set(MqOutboxEvent::getLastError, lastError)
                .set(MqOutboxEvent::getUpdatedAt, LocalDateTime.now())
                .eq(MqOutboxEvent::getId, id)
                .in(MqOutboxEvent::getStatus, OutboxStatus.NEW, OutboxStatus.FAIL)
                .update();
    }

    @Override
    public boolean markFail(Long id, int retryCnt, LocalDateTime nextRetryTime, String lastError) {
        return lambdaUpdate()
                .set(MqOutboxEvent::getStatus, OutboxStatus.FAIL)
                .set(MqOutboxEvent::getRetryCnt, retryCnt)
                .set(MqOutboxEvent::getNextRetryTime, nextRetryTime)
                .set(MqOutboxEvent::getLastError, lastError)
                .set(MqOutboxEvent::getUpdatedAt, LocalDateTime.now())
                .eq(MqOutboxEvent::getId, id)
                .in(MqOutboxEvent::getStatus, OutboxStatus.NEW, OutboxStatus.FAIL)
                .update();
    }
}

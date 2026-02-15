package com.meteor.ticketing.service;

import com.meteor.ticketing.domain.entity.MqOutboxEvent;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-15
 */
public interface IMqOutboxEventService extends IService<MqOutboxEvent> {

    List<MqOutboxEvent> listDueEvents(int limit);

    boolean markSent(Long id);

    boolean markExpired(Long id);

    boolean markDead(Long id, String lastError);

    boolean markFail(Long id, int retryCnt, LocalDateTime nextRetryTime, String lastError);

}

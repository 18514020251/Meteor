package com.meteor.analytics.service.impl;

import com.meteor.analytics.domain.entity.OpAnalyticsEventLog;
import com.meteor.analytics.mapper.OpAnalyticsEventLogMapper;
import com.meteor.analytics.service.IOpAnalyticsEventLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 运营统计事件去重表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
@Service
public class OpAnalyticsEventLogServiceImpl
        extends ServiceImpl<OpAnalyticsEventLogMapper, OpAnalyticsEventLog>
        implements IOpAnalyticsEventLogService {

    private static final String EVENT_TYPE_USER_REGISTERED = "USER_REGISTERED";

    @Override
    public boolean tryInsertUserRegistered(String eventId) {
        OpAnalyticsEventLog log = new OpAnalyticsEventLog();
        log.setEventType(EVENT_TYPE_USER_REGISTERED);
        log.setEventKey(eventId);
        log.setCreateTime(LocalDateTime.now());

        try {
            return this.save(log);
        } catch (DuplicateKeyException e) {
            return false;
        }
    }
}

package com.meteor.analytics.service;

import com.meteor.analytics.domain.entity.OpAnalyticsEventLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 运营统计事件去重表 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
public interface IOpAnalyticsEventLogService extends IService<OpAnalyticsEventLog> {

    /**
     * 尝试插入用户注册事件
     *
     * @param eventId 事件ID
     * @return 是否插入成功
     */
    boolean tryInsertUserRegistered(String eventId);
}

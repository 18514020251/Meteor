package com.meteor.analytics.service;

import com.meteor.analytics.domain.entity.OpAnalyticsDaily;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 运营分析-每日KPI汇总 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
public interface IOpAnalyticsDailyService extends IService<OpAnalyticsDaily> {

    /**
     *  统计注册数
     *
     * @param statDate 统计日期
     * @param occur 统计时间
     * */
    void incRegisterCntGlobal(LocalDate statDate, LocalDateTime occur);
}

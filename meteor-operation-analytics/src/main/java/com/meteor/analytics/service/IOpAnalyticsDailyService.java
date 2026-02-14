package com.meteor.analytics.service;

import com.meteor.analytics.controller.vo.*;
import com.meteor.analytics.domain.entity.OpAnalyticsDaily;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     *  查询注册趋势
     *
     * @return 注册趋势
     * */
    RegisterTrend7dVO queryRegisterTrend7dGlobal();

    /**
     *  查询支付尝试趋势
     *
     *  @return 支付尝试趋势
     * */
    Trend7dVO getPayAttemptTrend7dGlobal();

    /**
     *  查询今日支付
     *
     *  @return 今日支付
     * */
    PayTodayVO getPayTodayGlobal();

    /**
     *  查询GMV趋势
     *
     *  @param days 趋势天数
     *  @return GMV趋势
     * */
    TrendVO getGmvTrendGlobal(int days);

    /**
     *  查询重发状态数量
     *
     *  @return 重发状态数量
     * */
    List<ResendStateCountVO> getResendStateCounts();
}

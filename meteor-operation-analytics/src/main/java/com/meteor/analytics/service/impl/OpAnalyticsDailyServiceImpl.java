package com.meteor.analytics.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.meteor.analytics.controller.vo.PayTodayVO;
import com.meteor.analytics.controller.vo.RegisterTrend7dVO;
import com.meteor.analytics.controller.vo.Trend7dVO;
import com.meteor.analytics.controller.vo.TrendVO;
import com.meteor.analytics.domain.entity.OpAnalyticsDaily;
import com.meteor.analytics.enums.BizScopeEnum;
import com.meteor.analytics.mapper.OpAnalyticsDailyMapper;
import com.meteor.analytics.service.IOpAnalyticsDailyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.analytics.service.assembler.OpAnalyticsDailyAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * <p>
 * 运营分析-每日KPI汇总 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
@Service
@RequiredArgsConstructor
public class OpAnalyticsDailyServiceImpl
        extends ServiceImpl<OpAnalyticsDailyMapper, OpAnalyticsDaily>
        implements IOpAnalyticsDailyService {

    private static final String SCOPE_GLOBAL = "GLOBAL";
    private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");
    private final OpAnalyticsDailyAssembler dailyAssembler;
    private static final int DEFAULT_TREND_DAYS = 7;
    private static final int ALLOWED_DAYS_7 = 7;
    private static final int ALLOWED_DAYS_30 = 30;

    @Override
    public void incRegisterCntGlobal(LocalDate statDate, LocalDateTime calcTime) {

        boolean updated = incrGlobalRegisterCnt(statDate, calcTime);

        if (updated) {
            return;
        }

        OpAnalyticsDaily row = dailyAssembler.createGlobalDailyRow(statDate, calcTime);

        try {
            this.save(row);
        } catch (DuplicateKeyException e) {
            this.lambdaUpdate()
                    .eq(OpAnalyticsDaily::getBizScope, SCOPE_GLOBAL)
                    .eq(OpAnalyticsDaily::getBizId, BizScopeEnum.GLOBAL.getDefaultBizId())
                    .eq(OpAnalyticsDaily::getStatDate, statDate)
                    .setSql("register_cnt = register_cnt + 1")
                    .set(OpAnalyticsDaily::getCalcTime, calcTime != null ? calcTime : LocalDateTime.now())
                    .update();
        }
    }


    /**
     *  尝试更新全局注册数
     * */
    private boolean incrGlobalRegisterCnt(LocalDate statDate, LocalDateTime calcTime) {

        return this.lambdaUpdate()
                .eq(OpAnalyticsDaily::getBizScope, BizScopeEnum.GLOBAL)
                .eq(OpAnalyticsDaily::getBizId, BizScopeEnum.GLOBAL.getDefaultBizId())
                .eq(OpAnalyticsDaily::getStatDate, statDate)
                .setSql("register_cnt = register_cnt + 1")
                .set(OpAnalyticsDaily::getCalcTime,
                        calcTime != null ? calcTime : LocalDateTime.now())
                .update();
    }


    @Override
    public RegisterTrend7dVO queryRegisterTrend7dGlobal() {
        Trend7dVO vo = buildTrendInt(
                OpAnalyticsDaily::getRegisterCnt,
                q -> q.select(OpAnalyticsDaily::getStatDate, OpAnalyticsDaily::getRegisterCnt)
        );
        return new RegisterTrend7dVO(vo.dates(), vo.values());
    }

    @Override
    public Trend7dVO getPayAttemptTrend7dGlobal() {
        // 你现在前端用的是 deal_order_cnt（成交订单数），保持一致
        return buildTrendInt(
                OpAnalyticsDaily::getDealOrderCnt,
                q -> q.select(OpAnalyticsDaily::getStatDate, OpAnalyticsDaily::getDealOrderCnt)
        );
    }

    @Override
    public PayTodayVO getPayTodayGlobal() {
        LocalDate today = LocalDate.now();

        OpAnalyticsDaily row = baseGlobalDailyQuery()
                .eq(OpAnalyticsDaily::getStatDate, today)
                .select(
                        OpAnalyticsDaily::getPayAttemptCnt,
                        OpAnalyticsDaily::getPaySuccessCnt,
                        OpAnalyticsDaily::getGmvCent,
                        OpAnalyticsDaily::getDealOrderCnt
                )
                .one();

        if (row == null) {
            return PayTodayVO.ZERO;
        }

        int attempt = nzInt(row.getPayAttemptCnt());
        int success = nzInt(row.getPaySuccessCnt());
        long gmv = nzLong(row.getGmvCent());
        int deal = nzInt(row.getDealOrderCnt());

        return new PayTodayVO(attempt, success, gmv, deal);
    }

    @Override
    public TrendVO getGmvTrendGlobal(int days) {
        int n = normalizeDays(days);

        return buildTrendLong(
                n,
                OpAnalyticsDaily::getGmvCent,
                q -> q.select(OpAnalyticsDaily::getStatDate, OpAnalyticsDaily::getGmvCent)
        );
    }

    /* ====================== private helpers ====================== */

    /**
     * 统一的 GLOBAL 基础查询（避免每个方法重复 scope/bizId）
     */
    private com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<OpAnalyticsDaily> baseGlobalDailyQuery() {
        return this.lambdaQuery()
                .eq(OpAnalyticsDaily::getBizScope, BizScopeEnum.GLOBAL)
                .eq(OpAnalyticsDaily::getBizId, BizScopeEnum.GLOBAL.getDefaultBizId());
    }

    /**
     * 7d/nd 趋势（int 版本）
     */
    private Trend7dVO buildTrendInt(
            SFunction<OpAnalyticsDaily, Integer> valueGetter,
            UnaryOperator<LambdaQueryChainWrapper<OpAnalyticsDaily>> selector
    ) {
        int n = Math.max(1, DEFAULT_TREND_DAYS);
        DateRange range = DateRange.ofLastNDays(n);

        List<OpAnalyticsDaily> rows = selector.apply(
                baseGlobalDailyQuery()
                        .between(OpAnalyticsDaily::getStatDate, range.start(), range.end())
                        .orderByAsc(OpAnalyticsDaily::getStatDate)
        ).list();

        Map<LocalDate, Integer> map = new HashMap<>(rows.size() * 2);
        for (OpAnalyticsDaily r : rows) {
            LocalDate d = r.getStatDate();
            if (d != null) {
                map.put(d, nzInt(valueGetter.apply(r)));
            }
        }

        List<String> dates = new ArrayList<>(n);
        List<Integer> values = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            LocalDate d = range.start().plusDays(i);
            dates.add(d.format(MM_DD));
            values.add(map.getOrDefault(d, 0));
        }

        return new Trend7dVO(dates, values);
    }

    /**
     * nd 趋势（long 版本，给 GMV 用）
     */
    private TrendVO buildTrendLong(
            int days,
            SFunction<OpAnalyticsDaily, Long> valueGetter,
            UnaryOperator<LambdaQueryChainWrapper<OpAnalyticsDaily>> selector
    ) {
        int n = Math.max(1, days);
        DateRange range = DateRange.ofLastNDays(n);

        List<OpAnalyticsDaily> rows = selector.apply(
                baseGlobalDailyQuery()
                        .between(OpAnalyticsDaily::getStatDate, range.start(), range.end())
                        .orderByAsc(OpAnalyticsDaily::getStatDate)
        ).list();

        Map<LocalDate, Long> map = new HashMap<>(rows.size() * 2);
        for (OpAnalyticsDaily r : rows) {
            LocalDate d = r.getStatDate();
            if (d != null) {
                map.put(d, nzLong(valueGetter.apply(r)));
            }
        }

        List<String> dates = new ArrayList<>(n);
        List<Long> values = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            LocalDate d = range.start().plusDays(i);
            dates.add(d.format(MM_DD));
            values.add(map.getOrDefault(d, 0L));
        }

        return new TrendVO(dates, values);
    }

    private int normalizeDays(int days) {
        if (days == ALLOWED_DAYS_30) return ALLOWED_DAYS_30;
        return DEFAULT_TREND_DAYS;
    }

    private static int nzInt(Integer v) {
        return v == null ? 0 : v;
    }

    private static long nzLong(Long v) {
        return v == null ? 0L : v;
    }

    /**
     * 统一 last N days 的 start/end 计算（避免每个方法重复）
     */
    private record DateRange(LocalDate start, LocalDate end) {
        static DateRange ofLastNDays(int n) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(n - 1L);
            return new DateRange(start, end);
        }
    }


}

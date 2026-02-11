package com.meteor.analytics.service.impl;

import com.meteor.analytics.controller.vo.RegisterTrend7dVO;
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
    private static final int WEEKLY_DAYS = 7;

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

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        List<OpAnalyticsDaily> rows = this.lambdaQuery()
                .eq(OpAnalyticsDaily::getBizScope, BizScopeEnum.GLOBAL)
                .eq(OpAnalyticsDaily::getBizId, BizScopeEnum.GLOBAL.getDefaultBizId())
                .between(OpAnalyticsDaily::getStatDate, start, end)
                .orderByAsc(OpAnalyticsDaily::getStatDate)
                .list();

        Map<LocalDate, Integer> cntMap = new HashMap<>();
        for (OpAnalyticsDaily r : rows) {
            LocalDate d = r.getStatDate();
            Integer c = r.getRegisterCnt();
            if (d != null) {
                cntMap.put(d, c == null ? 0 : c);
            }
        }

        List<String> dates = new ArrayList<>(WEEKLY_DAYS);
        List<Integer> values = new ArrayList<>(WEEKLY_DAYS);

        for (int i = 0; i < WEEKLY_DAYS; i++) {
            LocalDate d = start.plusDays(i);
            dates.add(d.format(MM_DD));
            values.add(cntMap.getOrDefault(d, 0));
        }

        return new RegisterTrend7dVO(dates, values);
    }
}

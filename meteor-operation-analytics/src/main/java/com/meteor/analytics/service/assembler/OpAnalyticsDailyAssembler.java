package com.meteor.analytics.service.assembler;

import com.meteor.analytics.constants.AnalyticsDefaults;
import com.meteor.analytics.domain.entity.OpAnalyticsDaily;
import com.meteor.analytics.enums.BizScopeEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *  运营分析-每日KPI组装类
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 17:45
 */
@Component
public class OpAnalyticsDailyAssembler {

    /**
     * 构建 GLOBAL 维度的初始统计行
     */
    public OpAnalyticsDaily createGlobalDailyRow(
            LocalDate statDate,
            LocalDateTime calcTime
    ) {

        OpAnalyticsDaily row = new OpAnalyticsDaily();

        row.setStatDate(statDate);
        row.setBizScope(BizScopeEnum.GLOBAL);
        row.setBizId(BizScopeEnum.GLOBAL.getDefaultBizId());

        row.setRegisterCnt(AnalyticsDefaults.DEFAULT_CALC_VERSION);

        row.setCalcTime(resolveCalcTime(calcTime));

        row.setCalcVersion(AnalyticsDefaults.DEFAULT_CALC_VERSION);
        row.setSuccessRateBp(AnalyticsDefaults.DEFAULT_SUCCESS_RATE_BP);


        return row;
    }


    private LocalDateTime resolveCalcTime(LocalDateTime calcTime) {
        return calcTime != null ? calcTime : LocalDateTime.now();
    }
}

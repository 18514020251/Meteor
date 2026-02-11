package com.meteor.analytics.mq.assembler;

import com.meteor.analytics.constants.AnalyticsDefaults;
import com.meteor.analytics.domain.entity.OpAnalyticsDaily;
import com.meteor.analytics.enums.BizScopeEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *  运营分析-每日KPI汇总转换器
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 21:06
 */
@Component("serviceOpAnalyticsDailyAssembler")
public class OpAnalyticsDailyAssembler {

    public OpAnalyticsDaily createGlobalBaseRow(LocalDate statDate, LocalDateTime calcTime) {
        OpAnalyticsDaily row = new OpAnalyticsDaily();
        row.setStatDate(statDate);
        row.setBizScope(BizScopeEnum.GLOBAL);
        row.setBizId(BizScopeEnum.GLOBAL.getDefaultBizId());
        row.setCalcTime(calcTime);
        row.setCalcVersion(AnalyticsDefaults.DEFAULT_CALC_VERSION);
        row.setSuccessRateBp(AnalyticsDefaults.DEFAULT_SUCCESS_RATE_BP);
        return row;
    }

    /** 支付尝试：pay_attempt_cnt +1 */
    public OpAnalyticsDaily createGlobalPayAttemptIncRow(LocalDate statDate, LocalDateTime calcTime) {
        OpAnalyticsDaily row = createGlobalBaseRow(statDate, calcTime);
        row.setPayAttemptCnt(AnalyticsDefaults.EVENT_INC);
        return row;
    }

    /**
     * 支付成功：pay_success_cnt +1，deal_order_cnt +1，gmv += amountCent
     */
    public OpAnalyticsDaily createGlobalPaySuccessIncRow(LocalDate statDate, long amountCent, LocalDateTime calcTime) {
        OpAnalyticsDaily row = createGlobalBaseRow(statDate, calcTime);
        row.setPaySuccessCnt(AnalyticsDefaults.EVENT_INC);
        row.setDealOrderCnt(AnalyticsDefaults.EVENT_INC);
        row.setGmvCent(amountCent);
        return row;
    }
}

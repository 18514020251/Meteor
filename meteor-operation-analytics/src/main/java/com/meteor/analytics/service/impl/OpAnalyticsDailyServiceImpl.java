package com.meteor.analytics.service.impl;

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
    private final OpAnalyticsDailyAssembler dailyAssembler;

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
                    .eq(OpAnalyticsDaily::getBizId, 0L)
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
}

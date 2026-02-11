package com.meteor.analytics.mq.consumer;

import com.meteor.analytics.constants.AnalyticsEventTypes;
import com.meteor.analytics.domain.entity.OpAnalyticsDaily;
import com.meteor.analytics.domain.entity.OpAnalyticsEventLog;
import com.meteor.analytics.enums.BizScopeEnum;
import com.meteor.analytics.mq.assembler.OpAnalyticsDailyAssembler;
import com.meteor.analytics.mq.assembler.OpAnalyticsEventLogAssembler;
import com.meteor.analytics.service.IOpAnalyticsDailyService;
import com.meteor.analytics.service.IOpAnalyticsEventLogService;
import com.meteor.mq.contract.analytics.OperationAnalyticsContract;
import com.meteor.mq.contract.analytics.PaySuccessMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *  支付成功消息消费者
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 19:44
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaySuccessConsumer {

    private final IOpAnalyticsEventLogService eventLogService;
    private final IOpAnalyticsDailyService dailyService;
    private final OpAnalyticsDailyAssembler assembler;
    private final OpAnalyticsEventLogAssembler eventLogAssembler;

    @RabbitListener(
            queues = OperationAnalyticsContract.Queue.PAY_SUCCESS,
            errorHandler = "mqRejectErrorHandler"
    )
    @Transactional(rollbackFor = Exception.class)
    public void handle(PaySuccessMessage message) {

        if (message == null
                || message.getEventId() == null
                || message.getOrderNo() == null
                || message.getPayTime() == null) {
            return;
        }

        String eventId = message.getEventId();
        String orderNo = message.getOrderNo();
        long amountCent = message.getPayAmountCent() == null ? 0L : message.getPayAmountCent();
        LocalDateTime payTime = message.getPayTime();

        LocalDate statDate = payTime.toLocalDate();

        // NOTE: calcTime 的口径后续再统一（现在先用当前时间）
        LocalDateTime calcTime = LocalDateTime.now();

        if (!tryInsertEvent(eventId)) {
            return;
        }

        boolean updated = dailyService.lambdaUpdate()
                .eq(OpAnalyticsDaily::getBizScope, BizScopeEnum.GLOBAL)
                .eq(OpAnalyticsDaily::getBizId, BizScopeEnum.GLOBAL.getDefaultBizId())
                .eq(OpAnalyticsDaily::getStatDate, statDate)
                .setSql("pay_success_cnt = pay_success_cnt + 1")
                .setSql("deal_order_cnt = deal_order_cnt + 1")
                .setSql("gmv_cent = gmv_cent + " + amountCent)
                .set(OpAnalyticsDaily::getCalcTime, calcTime)
                .update();

        if (updated) {
            log.info("analytics pay_success applied, statDate={}, eventId={}, orderNo={}, gmv+={}",
                    statDate, eventId, orderNo, amountCent);
            return;
        }

        OpAnalyticsDaily row = assembler.createGlobalPaySuccessIncRow(statDate, amountCent, calcTime);
        dailyService.save(row);

        log.info("analytics pay_success inserted, statDate={}, eventId={}, orderNo={}, gmv={}",
                statDate, eventId, orderNo, amountCent);
    }

    private boolean tryInsertEvent(String key) {
        try {
            OpAnalyticsEventLog e = eventLogAssembler.build(AnalyticsEventTypes.PAY_SUCCESS, key);
            eventLogService.save(e);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }
}

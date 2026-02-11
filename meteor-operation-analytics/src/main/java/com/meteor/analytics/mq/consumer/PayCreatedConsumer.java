package com.meteor.analytics.mq.consumer;

import com.meteor.analytics.domain.entity.OpAnalyticsDaily;
import com.meteor.analytics.domain.entity.OpAnalyticsEventLog;
import com.meteor.analytics.enums.BizScopeEnum;
import com.meteor.analytics.mq.assembler.OpAnalyticsDailyAssembler;
import com.meteor.analytics.service.IOpAnalyticsDailyService;
import com.meteor.analytics.service.IOpAnalyticsEventLogService;
import com.meteor.mq.contract.analytics.OperationAnalyticsContract;
import com.meteor.mq.contract.analytics.PayCreatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *  支付单创建成功
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 19:19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayCreatedConsumer {

    private static final String EVENT_TYPE = "PAY_CREATED";

    private final IOpAnalyticsEventLogService eventLogService;
    private final IOpAnalyticsDailyService dailyService;
    private final OpAnalyticsDailyAssembler assembler;

    @RabbitListener(
            queues = OperationAnalyticsContract.Queue.PAY_CREATED,
            errorHandler = "mqRejectErrorHandler"
    )
    @Transactional(rollbackFor = Exception.class)
    public void handle(PayCreatedMessage message) {

        if (message == null || message.getEventId() == null || message.getOccurTime() == null) {
            return;
        }

        String eventId = message.getEventId();
        LocalDateTime occurTime = message.getOccurTime();
        LocalDate statDate = occurTime.toLocalDate();

        try {
            OpAnalyticsEventLog logRow = new OpAnalyticsEventLog();
            logRow.setEventType(EVENT_TYPE);
            logRow.setEventKey(eventId);
            logRow.setCreateTime(LocalDateTime.now());
            eventLogService.save(logRow);
        } catch (DuplicateKeyException e) {
            return;
        }

        LocalDateTime calcTime = LocalDateTime.now();

        boolean updated = dailyService.lambdaUpdate()
                .eq(OpAnalyticsDaily::getBizScope, BizScopeEnum.GLOBAL)
                .eq(OpAnalyticsDaily::getBizId, BizScopeEnum.GLOBAL.getDefaultBizId())
                .eq(OpAnalyticsDaily::getStatDate, statDate)
                .setSql("pay_attempt_cnt = pay_attempt_cnt + 1")
                .set(OpAnalyticsDaily::getCalcTime, calcTime)
                .update();

        if (updated) {
            log.info("analytics pay_attempt +1 applied, statDate={}, eventId={}, orderNo={}",
                    statDate, eventId, message.getOrderNo());
            return;
        }

        OpAnalyticsDaily row = assembler.createGlobalPayAttemptIncRow(statDate, calcTime);


        dailyService.save(row);

        log.info("analytics pay_attempt +1 inserted, statDate={}, eventId={}, orderNo={}",
                statDate, eventId, message.getOrderNo());
    }
}

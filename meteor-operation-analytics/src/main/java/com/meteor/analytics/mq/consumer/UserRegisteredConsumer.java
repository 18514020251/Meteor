package com.meteor.analytics.mq.consumer;

import com.meteor.analytics.service.IOpAnalyticsDailyService;
import com.meteor.analytics.service.IOpAnalyticsEventLogService;
import com.meteor.mq.contract.analytics.OperationAnalyticsContract;
import com.meteor.mq.contract.analytics.UserRegisteredMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final IOpAnalyticsEventLogService eventLogService;
    private final IOpAnalyticsDailyService dailyService;

    @RabbitListener(
            queues = OperationAnalyticsContract.Queue.USER_REGISTERED,
            errorHandler = "mqRejectErrorHandler"
    )
    @Transactional(rollbackFor = Exception.class)
    public void handle(UserRegisteredMessage message) {

        if (message == null || message.getEventId() == null || message.getUserId() == null) {
            return;
        }

        boolean first = eventLogService.tryInsertUserRegistered(message.getEventId());
        if (!first) {
            log.info("Analytics Register 事件已应用, eventId={}, userId={}",
                    message.getEventId(), message.getUserId());
            return;
        }

        LocalDateTime occur = message.getOccurTime() != null ? message.getOccurTime() : LocalDateTime.now();
        LocalDate statDate = occur.toLocalDate();

        dailyService.incRegisterCntGlobal(statDate, occur);

        log.info("analytics register +1 applied, statDate={}, eventId={}, userId={}",
                statDate, message.getEventId(), message.getUserId());
    }
}

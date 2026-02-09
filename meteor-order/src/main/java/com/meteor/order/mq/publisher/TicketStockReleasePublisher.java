package com.meteor.order.mq.publisher;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.ticketing.TicketOrderContract;
import com.meteor.mq.contract.ticketing.TicketStockReleaseMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *  票务库释放消息发布者
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 20:25
 */
@Component
@RequiredArgsConstructor
public class TicketStockReleasePublisher {

    private final MqSender mqSender;

    public void publishOrThrow(TicketStockReleaseMessage message) {

        MqSendResult result = mqSender.sendAndWaitConfirm(
                TicketOrderContract.Exchange.TICKET_ORDER,
                TicketOrderContract.RoutingKey.TICKET_STOCK_RELEASE,
                message,
                TicketOrderContract.CONFIRM_TIMEOUT
        );

        if (!result.isAck()) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MQ confirm failed");
        }
        if (result.noRoute()) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MQ NO_ROUTE");
        }
    }
}

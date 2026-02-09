package com.meteor.ticketing.mq.publisher;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.ticketing.TicketOrderContract;
import com.meteor.mq.contract.ticketing.TicketOrderDbReservedMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * DB扣库存成功 → 通知订单模块创建订单
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 17:33
 */
@Component
@RequiredArgsConstructor
public class TicketOrderDbReservedPublisher {

    private final MqSender mqSender;

    public void publishOrThrow(TicketOrderDbReservedMessage message) {

        MqSendResult result = mqSender.sendAndWaitConfirm(
                TicketOrderContract.Exchange.TICKET_ORDER,
                TicketOrderContract.RoutingKey.TICKET_ORDER_DB_RESERVED,
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

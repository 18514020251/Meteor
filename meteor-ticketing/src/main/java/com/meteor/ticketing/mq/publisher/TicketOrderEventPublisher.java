package com.meteor.ticketing.mq.publisher;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.ticketing.TicketOrderContract;
import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import com.meteor.ticketing.mq.assmabler.TicketOrderMessageAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *  抢票订单 MQ 发布者
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 16:22
 */
@Component
@RequiredArgsConstructor
public class TicketOrderEventPublisher {

    private final MqSender mqSender;
    private final TicketOrderMessageAssembler assembler;

    public void publishCreateOrThrow(String orderNo, Long userId, Long screeningId) {

        TicketOrderCreateMessage message = assembler.from(orderNo, userId, screeningId);

        MqSendResult result = mqSender.sendAndWaitConfirm(
                TicketOrderContract.Exchange.TICKET_ORDER,
                TicketOrderContract.RoutingKey.TICKET_ORDER_CREATE,
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

package com.meteor.ticketing.mq.assmabler;



import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 抢票订单消息组装器
 *
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 16:22
 */
@Component
public class TicketOrderMessageAssembler {

    public TicketOrderCreateMessage from(String orderNo, Long userId, Long screeningId) {
        TicketOrderCreateMessage msg = new TicketOrderCreateMessage();
        msg.setOrderNo(orderNo);
        msg.setUserId(userId);
        msg.setScreeningId(screeningId);
        msg.setCreateTime(LocalDateTime.now());
        return msg;
    }
}

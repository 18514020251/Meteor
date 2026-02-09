package com.meteor.order.mq.assembler;

import com.meteor.mq.contract.ticketing.TicketStockReleaseMessage;
import com.meteor.order.domain.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *  订单库存释放消息组装器
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 22:11
 */
@Component
public class TicketStockReleaseMessageAssembler {

    public TicketStockReleaseMessage from(String orderNo, OrderItem item, LocalDateTime now) {
        TicketStockReleaseMessage msg = new TicketStockReleaseMessage();
        msg.setOrderNo(orderNo);
        msg.setScreeningId(item.getScreeningId());
        msg.setTicketCount(item.getTicketCount());
        msg.setCreateTime(now);
        return msg;
    }
}

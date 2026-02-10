package com.meteor.order.service.assembler;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.order.domain.entity.Order;
import com.meteor.order.domain.entity.Payment;
import com.meteor.order.enums.PayChannelEnum;
import com.meteor.order.enums.PaymentStatusEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *  MQ → 订单领域对象转换器
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-10 20:39
 */
@Component
public class PaymentAssembler {

    public Payment buildInitPayment(
            String payNo,
            Order order,
            PayChannelEnum channel,
            Long operatorId,
            LocalDateTime now
    ) {

        Payment payment = new Payment();
        payment.setPayNo(payNo);
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setChannel(channel);
        payment.setStatus(PaymentStatusEnum.INIT);
        payment.setAmount(order.getPayAmount());
        payment.setCreateTime(now);
        payment.setUpdateTime(now);
        payment.setCreateBy(operatorId);
        payment.setUpdateBy(operatorId);
        payment.setDeleted(DeleteStatus.NORMAL);

        return payment;
    }
}

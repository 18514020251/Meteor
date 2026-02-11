package com.meteor.order.service.assembler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.order.domain.entity.Order;
import com.meteor.order.domain.entity.OrderOperateLog;
import com.meteor.order.domain.entity.Payment;
import com.meteor.order.enums.*;
import com.meteor.order.mapper.OrderOperateLogMapper;
import com.meteor.order.mapper.PaymentMapper;
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

    /**
     *  关闭支付单（仅 INIT->CLOSED，过期/关闭场景）
     * */
    public void tryClosePayment(PaymentMapper paymentMapper, String payNo, PaymentStatusEnum currentStatus, LocalDateTime now) {
        if (currentStatus != PaymentStatusEnum.INIT) {
            return;
        }
        paymentMapper.update(null,
                new LambdaUpdateWrapper<Payment>()
                        .set(Payment::getStatus, PaymentStatusEnum.CLOSED)
                        .set(Payment::getUpdateTime, now)
                        .eq(Payment::getPayNo, payNo)
                        .eq(Payment::getStatus, PaymentStatusEnum.INIT)
                        .eq(Payment::getDeleted, DeleteStatus.NORMAL)
        );
    }

    /**
     *  写入支付成功日志
     * */
    public void writePaySuccessLog(
            OrderOperateLogMapper operateLogMapper,
            Long orderId,
            String orderNo,
            Long uid,
            LocalDateTime now
    ) {
        OrderOperateLog log = new OrderOperateLog();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setFromStatus(OrderStatusEnum.WAIT_PAY);
        log.setToStatus(OrderStatusEnum.PAID);
        log.setOperateType(OrderOperateTypeEnum.PAY_SUCCESS);
        log.setOperatorType(OperatorTypeEnum.USER);
        log.setOperatorId(uid);
        log.setRemark("mock pay success");
        log.setCreateTime(now);
        log.setUpdateTime(now);
        log.setCreateBy(uid);
        log.setUpdateBy(uid);
        log.setDeleted(DeleteStatus.NORMAL);

        operateLogMapper.insert(log);
    }
}

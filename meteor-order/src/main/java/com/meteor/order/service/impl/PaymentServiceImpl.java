package com.meteor.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.analytics.PayCreatedMessage;
import com.meteor.mq.contract.analytics.PaySuccessMessage;
import com.meteor.order.constants.PayConstants;
import com.meteor.order.constants.PayQrConstants;
import com.meteor.order.controller.vo.pay.PayStatusVO;
import com.meteor.order.domain.entity.*;
import com.meteor.order.controller.vo.pay.PayCreateVO;
import com.meteor.order.enums.*;
import com.meteor.order.mapper.*;
import com.meteor.order.mq.publisher.OperationAnalyticsPublisher;
import com.meteor.order.service.IPaymentService;
import com.meteor.order.config.PaymentConfig;
import com.meteor.order.service.assembler.PaymentAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * <p>
 * 支付记录表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S3516")
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements IPaymentService {

    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final SnowflakeIdGenerator snowflake;
    private final OrderItemMapper orderItemMapper;
    private final OrderOperateLogMapper operateLogMapper;
    private final PaymentConfig paymentConfig;
    private final OperationAnalyticsPublisher operationAnalyticsPublisher;
    private final PaymentAssembler paymentAssembler;


    /**
     * 创建支付单（幂等）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateVO createPay(String orderNo, PayChannelEnum channel, Long uid) {

        validatePayChannel(channel);

        Order order = getOrderOrThrow(orderNo);
        assertOrderWaitPay(order);

        PayCreateVO reused = tryReuseInitPayment(order);
        if (reused != null) {
            return reused;
        }

        String payNo = PayConstants.PAY_NO_PREFIX + snowflake.nextId();
        LocalDateTime now = LocalDateTime.now();

        Payment payment = paymentAssembler.buildInitPayment(
                payNo,
                order,
                channel,
                uid,
                now
        );

        paymentMapper.insert(payment);

        bindPaymentToOrder(orderNo, payNo, channel, now);

        String eventId = PayConstants.PAY_TOTAL_MQ_PREFIX + snowflake.nextId();
        PayCreatedMessage msg = new PayCreatedMessage(
                eventId,
                orderNo,
                now
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                operationAnalyticsPublisher.publishPayCreated(msg);
            }
        });

        return new PayCreateVO(
                payNo,
                channel.getCode(),
                buildQrContent(payNo),
                order.getExpireTime()
        );
    }

    /**
     *  验证支付渠道
     * */
    private void validatePayChannel(PayChannelEnum channel) {
        if (channel == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "channel is null");
        }
        if (channel != PayChannelEnum.ALIPAY && channel != PayChannelEnum.WECHAT) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "invalid pay channel");
        }
    }

    /**
     *  订单待支付状态
     * */
    private void assertOrderWaitPay(Order order) {
        if (order.getStatus() != OrderStatusEnum.WAIT_PAY) {
            throw new BizException(CommonErrorCode.BIZ_ERROR, "order not in WAIT_PAY");
        }
    }


    /**
     *  尝试复用初始化状态的支付单
     * */
    private PayCreateVO tryReuseInitPayment(Order order) {
        String payNo = order.getPayNo();
        if (payNo == null || payNo.isBlank()) {
            return null;
        }

        Payment exist = getPaymentByPayNo(payNo);
        if (exist == null || exist.getStatus() != PaymentStatusEnum.INIT) {
            return null;
        }

        return new PayCreateVO(
                exist.getPayNo(),
                exist.getChannel().getCode(),
                buildQrContent(exist.getPayNo()),
                order.getExpireTime()
        );
    }




    /**
     *  获取订单（不存在则抛异常）
     * */
    private Order getOrderOrThrow(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "orderNo is blank");
        }
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getDeleted, DeleteStatus.NORMAL)
        );

        if (order == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "order not found");
        }
        return order;
    }

    /**
     *  绑定支付单到订单
     * */
    private void bindPaymentToOrder(
            String orderNo,
            String payNo,
            PayChannelEnum channel,
            LocalDateTime now
    ) {

        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .set(Order::getPayNo, payNo)
                        .set(Order::getPayChannel, channel)
                        .set(Order::getUpdateTime, now)
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getStatus, OrderStatusEnum.WAIT_PAY)
                        .eq(Order::getDeleted, DeleteStatus.NORMAL)
        );

        if (updated == 0) {
            throw new BizException(
                    CommonErrorCode.BIZ_ERROR,
                    "order not in WAIT_PAY when binding payment"
            );
        }
    }


    /**
     *  查询支付单
     * */
    private Payment getPaymentByPayNo(String payNo) {
        if (payNo == null || payNo.isBlank()) {
            return null;
        }
        return paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getPayNo, payNo)
                        .eq(Payment::getDeleted, DeleteStatus.NORMAL)
        );
    }


    /**
     *  构建支付二维码内容
     * */
    private String buildQrContent(String payNo) {
        return PayQrConstants.SCHEME
                + PayQrConstants.PAY_PATH
                + "?"
                + PayQrConstants.PAY_NO_PARAM
                + "="
                + payNo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPay(String payNo, String payPwd, Long uid) {

        validateConfirmParams(payNo, payPwd);

        LocalDateTime now = LocalDateTime.now();

        Payment payment = getPaymentOrThrow(payNo);
        if (payment.getStatus() == PaymentStatusEnum.SUCCESS) {
            return true;
        }
        assertPaymentInit(payment);

        Order order = getOrderByPaymentOrThrow(payment);
        assertOrderOwner(order, uid);

        if (order.getStatus() == OrderStatusEnum.PAID) {
            tryMarkPaymentSuccess(payNo, uid, now);
            return true;
        }

        markOrderPaidOrThrow(order.getOrderNo(), payNo, payment.getChannel(), uid, now);
        boolean changed = tryMarkPaymentSuccess(payNo, uid, now);

        markOrderItemPaid(order.getOrderNo(), uid, now);
        paymentAssembler.writePaySuccessLog(operateLogMapper , order.getId(), order.getOrderNo(), uid, now);

        if (changed) {
            publishPaySuccessAfterCommit(order.getOrderNo(), payment.getAmount(), now);
        }

        return true;
    }

    /**
     *  发送支付成功消息
     * */
    private void publishPaySuccessAfterCommit(String orderNo, Integer amountCent, LocalDateTime payTime) {

        String eventId = PayConstants.PAY_SUCCESS_MQ_PREFIX + snowflake.nextId();
        long amt = amountCent == null ? 0L : amountCent.longValue();

        PaySuccessMessage msg = new PaySuccessMessage(
                eventId,
                orderNo,
                amt,
                payTime
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                operationAnalyticsPublisher.publishPaySuccess(msg);
            }
        });
    }


    /**
     *  确认支付参数
     * */
    private void validateConfirmParams(String payNo, String payPwd) {
        if (payNo == null || payNo.isBlank()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "payNo is blank");
        }
        if (payPwd == null || payPwd.isBlank()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "payPwd is blank");
        }
        if (!paymentConfig.getMockPassword().equals(payPwd)) {
            throw new BizException(CommonErrorCode.BIZ_ERROR, "pay password incorrect");
        }
    }

    /**
     *  确认支付订单未支付
     * */
    private void assertPaymentInit(Payment payment) {
        if (payment.getStatus() != PaymentStatusEnum.INIT) {
            throw new BizException(CommonErrorCode.BIZ_ERROR, "payment not in INIT");
        }
    }

    /**
     *  获取订单
     * */
    private Order getOrderByPaymentOrThrow(Payment payment) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, payment.getOrderNo())
                        .eq(Order::getDeleted, DeleteStatus.NORMAL)
        );
        if (order == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "order not found");
        }
        return order;
    }

    private void assertOrderOwner(Order order, Long uid) {
        if (uid == null || !uid.equals(order.getUserId())) {
            throw new BizException(CommonErrorCode.FORBIDDEN, "order not belongs to current user");
        }
    }

    /**
     *   订单支付成功，修改订单状态为已支付
     * */
    private void markOrderPaidOrThrow(
            String orderNo,
            String payNo,
            PayChannelEnum channel,
            Long uid,
            LocalDateTime now
    ) {
        boolean updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .set(Order::getStatus, OrderStatusEnum.PAID)
                        .set(Order::getPayTime, now)
                        .set(Order::getPayChannel, channel)
                        .set(Order::getPayNo, payNo)
                        .set(Order::getUpdateTime, now)
                        .set(Order::getUpdateBy, uid)
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getStatus, OrderStatusEnum.WAIT_PAY)
                        .eq(Order::getDeleted, DeleteStatus.NORMAL)
        ) > 0;

        if (!updated) {
            throw new BizException(CommonErrorCode.BIZ_ERROR, "order not payable (maybe closed/timeout)");
        }
    }

    /**
     *  订单支付成功，修改订单状态为已支付
     * */
    private boolean tryMarkPaymentSuccess(String payNo, Long uid, LocalDateTime now) {
        return this.lambdaUpdate()
                .eq(Payment::getPayNo, payNo)
                .eq(Payment::getDeleted, DeleteStatus.NORMAL)
                .eq(Payment::getStatus, PaymentStatusEnum.INIT)
                .set(Payment::getStatus, PaymentStatusEnum.SUCCESS)
                .set(Payment::getPayTime, now)
                .set(Payment::getUpdateBy, uid)
                .set(Payment::getUpdateTime, now)
                .update();
    }


    /**
     *  订单支付成功，修改订单项状态为已支付
     * */
    private void markOrderItemPaid(String orderNo, Long uid, LocalDateTime now) {
        orderItemMapper.update(null,
                new LambdaUpdateWrapper<OrderItem>()
                        .set(OrderItem::getStatus, OrderStatusEnum.PAID)
                        .set(OrderItem::getUpdateTime, now)
                        .set(OrderItem::getUpdateBy, uid)
                        .eq(OrderItem::getOrderNo, orderNo)
                        .eq(OrderItem::getDeleted, DeleteStatus.NORMAL)
                        .eq(OrderItem::getStatus, OrderStatusEnum.WAIT_PAY)
        );
    }


    @Override
    public PayStatusVO getPayStatus(String payNo) {

        validatePayNo(payNo);

        Payment payment = getPaymentOrThrow(payNo);
        Order order = getOrderByPaymentOrThrow(payment);

        LocalDateTime now = LocalDateTime.now();

        if (order.getStatus() == OrderStatusEnum.PAID) {
            tryMarkPaymentSuccess(payNo, order.getPayTime(), now);
            return new PayStatusVO(PaymentStatusEnum.SUCCESS.getCode(), payNo, order.getOrderNo());
        }

        if (isExpired(order, now)) {
            paymentAssembler.tryClosePayment(paymentMapper , payNo, payment.getStatus(), now);
            return new PayStatusVO(PaymentStatusEnum.CLOSED.getCode(), payNo, order.getOrderNo());
        }

        return new PayStatusVO(payment.getStatus().getCode(), payNo, order.getOrderNo());
    }

    /**
     *  参数校验
     * */
    private void validatePayNo(String payNo) {
        if (payNo == null || payNo.isBlank()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "payNo is blank");
        }
    }

    /**
     *  获取支付信息
     * */
    private Payment getPaymentOrThrow(String payNo) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getPayNo, payNo)
                        .eq(Payment::getDeleted, DeleteStatus.NORMAL)
        );
        if (payment == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "payment not found");
        }
        return payment;
    }

    /**
     *  过期判断
     * */
    private boolean isExpired(Order order, LocalDateTime now) {
        return order.getExpireTime() != null && now.isAfter(order.getExpireTime());
    }

    /**
     *  订单支付成功，修改支付状态为成功
     * */
    private void tryMarkPaymentSuccess(String payNo, LocalDateTime orderPayTime, LocalDateTime now) {
        paymentMapper.update(null,
                new LambdaUpdateWrapper<Payment>()
                        .set(Payment::getStatus, PaymentStatusEnum.SUCCESS)
                        .set(Payment::getPayTime, orderPayTime != null ? orderPayTime : now)
                        .set(Payment::getUpdateTime, now)
                        .eq(Payment::getPayNo, payNo)
                        .eq(Payment::getStatus, PaymentStatusEnum.INIT)
                        .eq(Payment::getDeleted, DeleteStatus.NORMAL)
        );
    }
}

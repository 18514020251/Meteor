package com.meteor.order.service.impl;

import com.meteor.order.domain.entity.Payment;
import com.meteor.order.mapper.PaymentMapper;
import com.meteor.order.service.IPaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付记录表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements IPaymentService {

}

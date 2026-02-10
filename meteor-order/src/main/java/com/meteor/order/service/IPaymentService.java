package com.meteor.order.service;

import com.meteor.order.controller.vo.pay.PayCreateVO;
import com.meteor.order.controller.vo.pay.PayStatusVO;
import com.meteor.order.domain.entity.Payment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.order.enums.PayChannelEnum;

/**
 * <p>
 * 支付服务
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
public interface IPaymentService extends IService<Payment> {

    /**
     * 创建支付单（幂等）
     *
     * @param orderNo 订单号
     * @param channel 1=ALIPAY 2=WECHAT
     * @param uid     当前登录用户
     */
    PayCreateVO createPay(String orderNo, PayChannelEnum channel, Long uid);

    /**
     * 模拟支付确认（幂等）
     *
     * @param payNo  支付单号
     * @param payPwd 支付密码（演示用）
     * @param uid    当前登录用户
     * @return 是否支付成功
     */
    boolean confirmPay(String payNo, String payPwd, Long uid);

    /**
     * 查询支付状态（轮询用）
     */
    PayStatusVO getPayStatus(String payNo);
}

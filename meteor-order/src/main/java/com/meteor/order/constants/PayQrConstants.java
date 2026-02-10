package com.meteor.order.constants;

/**
 *  支付二维码常量
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-10 20:37
 */
public final class PayQrConstants {

    private PayQrConstants() {}

    /** 支付二维码协议 scheme */
    public static final String SCHEME = "meteor-pay://";

    /** 支付路径 */
    public static final String PAY_PATH = "pay";

    /** 支付单号参数名 */
    public static final String PAY_NO_PARAM = "payNo";
}

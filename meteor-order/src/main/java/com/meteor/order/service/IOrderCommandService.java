package com.meteor.order.service;

import java.time.LocalDateTime;

/**
 * 订单领域服务接口
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 22:30
 */
public interface IOrderCommandService {
    boolean existsByOrderNo(String orderNo);

    /**
     * 超时关单：仅当当前为 WAIT_PAY 才会更新
     * @return true=成功关单，false=未关单（不存在/已支付/已关闭）
     */
    boolean closeTimeout(String orderNo, LocalDateTime now);
}

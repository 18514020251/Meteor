package com.meteor.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "programmer.payment")
public class PaymentConfig {

    /**
     * 模拟支付密码
     */
    private String mockPassword;
}
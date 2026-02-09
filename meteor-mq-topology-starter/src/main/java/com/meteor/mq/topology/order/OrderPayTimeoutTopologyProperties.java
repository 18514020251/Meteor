package com.meteor.mq.topology.order;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *  订单支付超时拓扑配置
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 19:09
 */
@Data
@ConfigurationProperties(prefix = "meteor.mq.topology.order-pay")
public class OrderPayTimeoutTopologyProperties {
    private boolean enabled = true;

}

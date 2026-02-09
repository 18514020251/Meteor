package com.meteor.mq.topology.ticketing;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 抢票订单拓扑配置属性
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 12:02
 */
@Getter
@ConfigurationProperties(prefix = "meteor.mq.topology.ticket-order")
public class TicketOrderTopologyProperties {

    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

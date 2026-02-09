package com.meteor.mq.topology.ticketing;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 抢票订单拓扑配置属性
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 12:02
 */
@Data
@ConfigurationProperties(prefix = "meteor.mq.topology.ticket-order")
public class TicketOrderTopologyProperties {

    private boolean enabled = true;

}

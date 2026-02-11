package com.meteor.mq.topology.analytics;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 运营分析拓扑配置
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11
 */
@Data
@ConfigurationProperties(prefix = "meteor.mq.topology.analytics")
public class OperationAnalyticsTopologyProperties {

    /**
     * 是否启用拓扑声明
     */
    private boolean enabled = true;
}

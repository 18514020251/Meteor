package com.meteor.mq.topology.message;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *  消息拓扑配置属性
 *
 * @author Programmer
 * @date 2026-01-29 21:19
 */
@Getter
@ConfigurationProperties(prefix = "meteor.mq.topology.user-message")
public class UserMessageTopologyProperties {

    private boolean enabled = true;

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

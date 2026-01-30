package com.meteor.mq.topology.message;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Programmer
 * @date 2026-01-29 21:19
 */
@ConfigurationProperties(prefix = "meteor.mq.topology.user-message")
public class UserMessageTopologyProperties {

    private boolean enabled = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

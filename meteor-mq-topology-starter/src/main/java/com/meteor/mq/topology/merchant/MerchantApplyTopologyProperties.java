package com.meteor.mq.topology.merchant;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *  商家申请拓扑属性
 *
 * @author Programmer
 * @date 2026-01-28 11:15
 */
@ConfigurationProperties(prefix = "meteor.mq.topology.merchant-apply")
public class MerchantApplyTopologyProperties {
    /**
     * 是否启用商家申请拓扑声明
     */
    private boolean enabled = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}


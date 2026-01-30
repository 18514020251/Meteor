package com.meteor.mq.topology.merchant;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *  商家申请拓扑属性
 *
 * @author Programmer
 * @date 2026-01-28 11:15
 */
@Getter
@ConfigurationProperties(prefix = "meteor.mq.topology.merchant-apply")
public class MerchantApplyTopologyProperties {

    private boolean enabled = true;

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}


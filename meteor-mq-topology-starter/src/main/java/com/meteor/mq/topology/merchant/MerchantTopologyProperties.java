package com.meteor.mq.topology.merchant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 商家 MQ 拓扑配置属性
 *
 * 控制是否在当前应用启动时声明 merchant 相关的 exchange / queue / dlq
 *
 * @author Programmer
 */
@Data
@ConfigurationProperties(prefix = "meteor.mq.topology.merchant")
public class MerchantTopologyProperties {

    /**
     * 是否启用商家 MQ 拓扑声明
     */
    private boolean enabled = true;
}


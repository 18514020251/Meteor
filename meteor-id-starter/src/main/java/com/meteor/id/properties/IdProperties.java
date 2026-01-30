package com.meteor.id.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Programmer
 * @date 2026-01-30 16:28
 */
@Data
@ConfigurationProperties(prefix = "meteor.id")
public class IdProperties {
    private long workerId = 1;
    private long datacenterId = 1;
}

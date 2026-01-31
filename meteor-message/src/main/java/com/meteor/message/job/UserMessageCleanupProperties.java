package com.meteor.message.job;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Programmer
 * @date 2026-01-31 10:52
 */
@Data
@Component
@ConfigurationProperties(prefix = "message.cleanup")
public class UserMessageCleanupProperties {

    /**
     * 每批删除数量
     */
    private int batchSize = 1000;

    /**
     * 每次调度最多执行的批次数
     */
    private int maxRounds = 5;

    /**
     * 保留天数（超过即清理）
     */
    private int retentionDays = 15;
}

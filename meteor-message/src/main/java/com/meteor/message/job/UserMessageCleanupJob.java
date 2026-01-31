package com.meteor.message.job;

import com.meteor.message.mapper.UserMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *  用户消息清理任务
 *
 * @author Programmer
 * @date 2026-01-31 10:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserMessageCleanupJob {

    private final UserMessageMapper userMessageMapper;
    private final UserMessageCleanupProperties properties;

    @Scheduled(fixedDelayString = "${message.cleanup.fixed-delay-ms:1000}")
    public void cleanup() {
        LocalDateTime cutoff =
                LocalDateTime.now().minusDays(properties.getRetentionDays());

        int total = 0;

        for (int round = 1; round <= properties.getMaxRounds(); round++) {
            int affected = userMessageMapper.physicalDeleteExpired(
                    cutoff,
                    properties.getBatchSize()
            );
            total += affected;

            if (affected < properties.getBatchSize()) {
                break;
            }
        }

        if (total > 0) {
            log.info(
                    "user_message cleanup done, cutoff={}, maxRounds={}, batchSize={}, deletedRows={}",
                    cutoff,
                    properties.getMaxRounds(),
                    properties.getBatchSize(),
                    total
            );
        }
    }
}


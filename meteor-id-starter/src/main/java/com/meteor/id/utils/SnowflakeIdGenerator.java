package com.meteor.id.utils;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;

/**
 *  雪花ID生成器
 *
 * @author Programmer
 * @date 2026-01-30 16:09
 */
public class SnowflakeIdGenerator {

    private static final long START_TIMESTAMP = 1704067200000L;

    private static final long SEQUENCE_BITS = 12;
    private static final long MACHINE_BITS  = 5;
    private static final long DATACENTER_BITS = 5;

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS + DATACENTER_BITS;

    private final long datacenterId;
    private final long machineId;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }
    /**
     * 生成下一个 ID（线程安全）
     */
    public synchronized long nextId() {
        long currentTimestamp = currentTime();

        if (currentTimestamp < lastTimestamp) {
            throw new BizException(CommonErrorCode.TIME_REVERSED);
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = currentTime();
        }
        return currentTimestamp;
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }
}


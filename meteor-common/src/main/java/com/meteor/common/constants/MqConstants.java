package com.meteor.common.constants;

/**
 *  MQ常量
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 11:36
 */
public final class MqConstants {

    // 默认重试次数
    public static final int DEFAULT_RETRY_COUNT = 0;
    // 默认下次重试时间(单位:分钟)
    public static final long DEFAULT_NEXT_RETRY_TIME = 5L;
    // 默认最大重试次数
    public static final int DEFAULT_RETRY_COUNT_MAX = 3;

    private MqConstants() {
    }
}

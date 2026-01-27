package com.meteor.common.mq.constants;

import java.time.Duration;

/**
 * MQ 常量
 *
 * @author Programmer
 * @date 2026-01-27 19:11
 */
public class MqConstants {

    private MqConstants(){}

    public static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(3);

}

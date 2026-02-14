package com.meteor.user.domain.cmd;

import com.meteor.common.enums.system.ModuleEnum;

import java.time.Duration;

/**
 *  MQ发送命令
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 23:15
 */
public record MqSendCmd(
        String msgId,
        ModuleEnum module,
        Long bizId,
        String exchange,
        String routingKey,
        String topic,
        Object payload,
        Duration timeout,
        boolean throwOnFail
) {}

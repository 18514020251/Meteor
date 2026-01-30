package com.meteor.mq.core;

import lombok.Getter;
import org.springframework.amqp.core.ReturnedMessage;

/**
 *  MQ 发送结果
 *
 * @author Programmer
 * @date 2026-01-27 15:26
 */
@Getter
public class MqSendResult {

    private final boolean ack;
    private final String cause;
    private final ReturnedMessage returnedMessage;

    public MqSendResult(boolean ack, String cause, ReturnedMessage returnedMessage) {
        this.ack = ack;
        this.cause = cause;
        this.returnedMessage = returnedMessage;
    }

    public boolean noRoute() {
        return returnedMessage != null;
    }

    public static MqSendResult failed() {
        return new MqSendResult(false, "SEND_FAILED", null);
    }
}

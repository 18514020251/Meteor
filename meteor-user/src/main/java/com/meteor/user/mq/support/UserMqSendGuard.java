package com.meteor.user.mq.support;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import com.meteor.user.domain.cmd.MqSendCmd;
import com.meteor.user.service.IUserMqFailMsgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *  MQ发送失败处理
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 15:01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserMqSendGuard {

    private final MqSender mqSender;
    private final IUserMqFailMsgService failService;
    private final UserMqFailMsgBuilder builder;

    public MqSendResult send(MqSendCmd cmd) {

        MqSendResult result = mqSender.sendAndWaitConfirm(
                cmd.exchange(), cmd.routingKey(), cmd.payload(), cmd.timeout()
        );

        String error = resolveSendError(result);

        if (error != null) {
            log.error("MQ send failed: msgId={}, exchange={}, routingKey={}, error={}",
                    cmd.msgId(), cmd.exchange(), cmd.routingKey(), error);

            // 失败落库：这里用 cmd.exchange()，别写死
            try {
                failService.save(
                        builder.build(
                                UserMqFailBuildCmd.builder()
                                        .msgId(cmd.msgId())
                                        .module(cmd.module())
                                        .bizId(cmd.bizId())
                                        .exchange(cmd.exchange())
                                        .routingKey(cmd.routingKey())
                                        .topic(cmd.topic())
                                        .payload(cmd.payload())
                                        .errorMsg(error)
                                        .build()
                        )
                );
            } catch (Exception e) {
                // 这里如果你想更精准，建议只 catch DuplicateKeyException
                log.warn("mq fail msg save failed or already exists, msgId={}", cmd.msgId(), e);
            }

            if (cmd.throwOnFail()) {
                throw new BizException(CommonErrorCode.MQ_SEND_FAILED);
            }
        }

        return result;
    }

    /**
     * 统一判定发送错误原因：避免三元/嵌套判断 & 处理 null
     */
    private String resolveSendError(MqSendResult result) {
        if (result == null) {
            return "MQ sendAndWaitConfirm returned null";
        }
        // 通常 NO_ROUTE 比 ack 更“关键”，先判它更直观
        if (result.noRoute()) {
            return "MQ NO_ROUTE";
        }
        if (!result.isAck()) {
            return "MQ confirm failed";
        }
        return null;
    }
}

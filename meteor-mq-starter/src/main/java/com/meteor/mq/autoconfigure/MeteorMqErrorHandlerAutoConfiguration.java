package com.meteor.mq.autoconfigure;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

/**
 *  MQ 错误处理自动配置
 *
 * @author Programmer
 */
@Slf4j
@AutoConfiguration
public class MeteorMqErrorHandlerAutoConfiguration {

    @Bean("mqRejectErrorHandler")
    @ConditionalOnMissingBean(name = "mqRejectErrorHandler")
    public RabbitListenerErrorHandler mqRejectErrorHandler() {
        return (amqpMessage, message, exception) -> {

            Throwable root = rootCause(exception);

            if (root instanceof BizException be
                    && be.getCode() == CommonErrorCode.INVALID_MQ_MESSAGE.getCode()) {

                log.warn("Reject invalid MQ message: {}", safePayload(message), exception);
                throw exception;
            }

            log.error("MQ consume failed: {}", safePayload(message), exception);
            throw exception;
        };
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur;
    }

    private static Object safePayload(Message<?> message) {
        return message == null ? null : message.getPayload();
    }
}

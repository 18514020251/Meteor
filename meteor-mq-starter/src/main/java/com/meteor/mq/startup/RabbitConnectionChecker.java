package com.meteor.mq.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 *  RabbitMQ 启动连接测试
 *
 * @author Programmer
 * @date 2026-01-28 19:55
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitConnectionChecker implements ApplicationListener<ApplicationReadyEvent> {

    private static final String UNKNOWN = "unknown";

    private final ConnectionFactory connectionFactory;
    private final Environment env;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String host = env.getProperty("spring.rabbitmq.host", UNKNOWN);
        String port = env.getProperty("spring.rabbitmq.port", UNKNOWN);
        String vhost = env.getProperty("spring.rabbitmq.virtual-host", "/");
        String username = env.getProperty("spring.rabbitmq.username", UNKNOWN);

        if (connectionFactory == null) {
            log.warn("""
                ======== Middleware Check (mq-starter) ========
                RabbitMQ: FAIL
                Reason : ConnectionFactory is null
                Addr   : {}:{}
                VHost  : {}
                User   : {}
                ===============================================
                """, host, port, vhost, username);
            return;
        }

        try (Connection conn = connectionFactory.createConnection()) {
            boolean open = conn.isOpen();

            log.info("""
                ======== Middleware Check (mq-starter) ========
                RabbitMQ: OK
                Addr   : {}:{}
                VHost  : {}
                User   : {}
                Open   : {}
                ===============================================
                """, host, port, vhost, username, open);

        } catch (Exception e) {
            log.warn("""
                ======== Middleware Check (mq-starter) ========
                RabbitMQ: FAIL
                Addr   : {}:{}
                VHost  : {}
                User   : {}
                ERR    : {}
                ===============================================
                """, host, port, vhost, username, rootMessage(e));
        }
    }

    private String rootMessage(Throwable t) {
        Throwable cur = Objects.requireNonNull(t);
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur.getClass().getSimpleName() + ": " + cur.getMessage();
    }
}

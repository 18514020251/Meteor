package com.meteor.user.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.meteor.common.constants.SpringPropertyKeys.*;

/**
 * Redis 连接测试
 *
 * @author Programmer
 * @date 2026-01-26 21:32
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisConnectionChecker implements ApplicationListener<ApplicationReadyEvent> {

    private final StringRedisTemplate stringRedisTemplate;
    private final Environment env;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String host = env.getProperty(REDIS_HOST, "unknown");
        String port = env.getProperty(REDIS_PORT, "unknown");
        String db = env.getProperty(REDIS_DB, REDIS_DEFAULT_DB);

        RedisConnectionFactory factory = stringRedisTemplate.getConnectionFactory();
        if (factory == null) {
            log.warn("""
                ======== Middleware Check (meteor-user) ========
                Redis:  FAIL
                Reason: ConnectionFactory is null
                Addr :  {}:{}
                DB   :  {}
                ===============================================
                """, host, port, db);
            return;
        }

        try (RedisConnection conn = factory.getConnection()) {
            String pong = conn.ping();

            log.info("""
                ======== Middleware Check (meteor-user) ========
                Redis:  OK
                Addr :  {}:{}
                DB   :  {}
                PING :  {}
                ===============================================
                """, host, port, db, pong);

        } catch (Exception e) {
            log.warn("""
                ======== Middleware Check (meteor-user) ========
                Redis:  FAIL
                Addr :  {}:{}
                DB   :  {}
                ERR  :  {}
                ===============================================
                """, host, port, db, rootMessage(e));
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

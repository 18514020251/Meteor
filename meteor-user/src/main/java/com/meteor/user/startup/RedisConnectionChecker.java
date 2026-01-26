package com.meteor.user.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import static com.meteor.common.constants.SpringPropertyKeys.*;

/**
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
        String host = env.getProperty(REDIS_HOST);
        String port = env.getProperty(REDIS_PORT);
        String db = env.getProperty(REDIS_DB);
        if (db == null) {
            db = REDIS_DEFAULT_DB;
        }


        try {
            assert stringRedisTemplate.getConnectionFactory() != null;
            String pong = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();

            log.info("""
        ======== Middleware Check (meteor-user) ========
        Redis:  OK
        Addr :  {}:{}
        DB   :  {}
        PING :  {}
        ===============================================
        """, host, port, db, pong);

        } catch (Exception e) {
            log.info("""
        ======== Middleware Check (meteor-user) ========
        Redis:  FAIL
        Addr :  {}:{}
        DB   :  {}
        PING :  {}
        ===============================================
        """, host, port, db, rootMessage(e));

        }
    }

    private String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur.getClass().getSimpleName() + ": " + cur.getMessage();
    }
}

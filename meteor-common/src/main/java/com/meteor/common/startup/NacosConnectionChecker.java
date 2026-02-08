package com.meteor.common.startup;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Properties;

/**
 * Nacos 启动连接检测
 *
 * @author Programmer
 */
@Slf4j
@RequiredArgsConstructor
public class NacosConnectionChecker implements ApplicationListener<ApplicationReadyEvent> {

    private static final String UNKNOWN = "unknown";

    private final Environment env;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        String serverAddr = env.getProperty("spring.cloud.nacos.config.server-addr");
        String namespace  = env.getProperty("spring.cloud.nacos.config.namespace", "");
        String group      = env.getProperty("spring.cloud.nacos.config.group", "DEFAULT_GROUP");

        if (!StringUtils.hasText(serverAddr)) {
            log.info("""
                ======== Middleware Check (nacos) ========
                Nacos: SKIP
                Reason: nacos not enabled
                =========================================
                """);
            return;
        }

        try {
            Properties props = new Properties();
            props.put("serverAddr", serverAddr);
            if (StringUtils.hasText(namespace)) {
                props.put("namespace", namespace);
            }

            ConfigService configService = NacosFactory.createConfigService(props);

            configService.getConfig(
                    "nacos-health-check",
                    group,
                    1000
            );

            log.info("""
                ======== Middleware Check (nacos) ========
                Nacos: OK
                Addr : {}
                Group: {}
                NS   : {}
                =========================================
                """, serverAddr, group,
                    StringUtils.hasText(namespace) ? namespace : "public");

        } catch (Exception e) {
            log.warn("""
                ======== Middleware Check (nacos) ========
                Nacos: FAIL
                Addr : {}
                Group: {}
                NS   : {}
                ERR  : {}
                =========================================
                """, serverAddr, group,
                    StringUtils.hasText(namespace) ? namespace : "public",
                    rootMessage(e));
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

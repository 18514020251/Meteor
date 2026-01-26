package com.meteor.gateway;

import com.meteor.common.utils.PrintMeteor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关模块启动类
 *
 * @author Programmer
 * @date 2026-01-16 22:38
 */
@EnableDiscoveryClient
@SpringBootApplication
@SuppressWarnings("squid:S1135")
public class MeteorGatewayApplication {
    // TODO: 增加网关模块 Redis 限流

    static {
        // 系统级禁用 Nacos gRPC
        System.setProperty("nacos.naming.rpc.grpc.enabled", "false");
        System.setProperty("nacos.core.protocol.rsocket.enabled", "false");
        System.setProperty("nacos.core.protocol.http.enabled", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(MeteorGatewayApplication.class, args);
        PrintMeteor.printWelcomeBanner();
    }
}


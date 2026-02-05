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
public class MeteorGatewayApplication {
    // NOTE:meteor-api-contract 模块待增加
    public static void main(String[] args) {
        SpringApplication.run(MeteorGatewayApplication.class, args);
        PrintMeteor.printWelcomeBanner();
    }
}


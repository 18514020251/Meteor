package com.meteor.gateway;

import com.meteor.common.utils.PrintMeteor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关模块启动类
 *
 * @author Programmer
 * @date 2026-01-16 22:38
 */
@SpringBootApplication
public class MeteorGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeteorGatewayApplication.class, args);
        PrintMeteor.printWelcomeBanner();
    }
}


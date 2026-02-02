package com.meteor.ticketing;

import com.meteor.common.utils.PrintMeteor;
import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *   票务模块启动类
 *
 * @author Programmer
 * @date 2026-02-02 10:48
 */
@EnableMeteorMyBatisPlus
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.meteor.ticketing.mapper")
public class TicketingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketingApplication.class, args);
        PrintMeteor.printWelcomeBanner();
    }
}

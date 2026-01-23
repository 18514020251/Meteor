package com.meteor.user;

import com.meteor.common.utils.PrintMeteor;
import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户模块启动类
 *
 * @author Programmer
 * @date 2026-01-16 12:35
 */
@EnableMeteorMyBatisPlus
@EnableDiscoveryClient
@SpringBootApplication
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        PrintMeteor.printWelcomeBanner();
    }

}

package com.meteor.admin;

import com.meteor.common.utils.PrintMeteor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *  后台管理服务启动类
 *
 * @author Programmer
 * @date 2026-01-23 10:12
 */
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.meteor.admin.mapper")
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
        PrintMeteor.printWelcomeBanner();
    }
}

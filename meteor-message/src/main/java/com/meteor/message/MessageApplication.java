package com.meteor.message;

import com.meteor.common.utils.PrintMeteor;
import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *  message 模块启动类
 *
 * @author Programmer
 * @date 2026-01-29 17:30
 */
@EnableMeteorMyBatisPlus
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.meteor.message.mapper")
public class MessageApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageApplication.class , args);
        PrintMeteor.printWelcomeBanner();
    }
}

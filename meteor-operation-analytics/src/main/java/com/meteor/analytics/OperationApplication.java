package com.meteor.analytics;

import com.meteor.common.utils.PrintMeteor;
import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *  数据分析模块启动类
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 9:34
 */
@EnableScheduling
@EnableFeignClients(basePackages = "com.meteor.api.contract")
@SpringBootApplication
@EnableMeteorMyBatisPlus
@MapperScan("com.meteor.analytics.mapper")
public class OperationApplication {
    public static void main(String[] args) {
        SpringApplication.run(OperationApplication.class, args);
        PrintMeteor.printWelcomeBanner();
    }
}

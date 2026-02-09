package com.meteor.order;

import com.meteor.common.utils.PrintMeteor;
import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *  订单服务启动类
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 15:47
 */
@EnableMeteorMyBatisPlus
@SpringBootApplication
@MapperScan("com.meteor.order.mapper")
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        PrintMeteor.printWelcomeBanner();
    }
}

package com.meteor.product;

import com.meteor.common.utils.PrintMeteor;
import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *  商品服务启动类
 *
 * @author Programmer
 * @date 2026-02-01 15:16
 */
@EnableMeteorMyBatisPlus
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.meteor.product.mapper")
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class , args);
        PrintMeteor.printWelcomeBanner();
    }
}

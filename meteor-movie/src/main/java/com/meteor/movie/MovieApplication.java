package com.meteor.movie;

import com.meteor.common.utils.PrintMeteor;
import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Programmer
 * @date 2026-02-02 16:26
 */
@EnableMeteorMyBatisPlus
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.meteor.movie.mapper")
@EnableFeignClients(basePackages = "com.meteor.movie.client")
public class MovieApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieApplication.class , args);
        PrintMeteor.printWelcomeBanner();
    }
}

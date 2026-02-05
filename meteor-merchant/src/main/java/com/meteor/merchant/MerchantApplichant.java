package com.meteor.merchant;

import com.meteor.common.utils.PrintMeteor;
import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商家模块启动类
 *
 * @author Programmer
 * @date 2026-01-31 15:41
 */
@EnableFeignClients(clients = {
        com.meteor.api.contract.user.client.UserClient.class
})
@EnableMeteorMyBatisPlus
@SpringBootApplication
@MapperScan("com.meteor.merchant.mapper")
public class MerchantApplichant {
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplichant.class, args);
        PrintMeteor.printWelcomeBanner();
    }
}

package com.meteor.merchant;

import com.meteor.common.utils.PrintMeteor;
import com.meteor.mp.annotation.EnableMeteorMyBatisPlus;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商家模块启动类
 *
 * @author Programmer
 * @date 2026-01-31 15:41
 */
@EnableMeteorMyBatisPlus
@SpringBootApplication
@MapperScan("com.meteor.merchant.mapper")
public class MerchantApplichant {
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplichant.class, args);
        PrintMeteor.printWelcomeBanner();
    }
}

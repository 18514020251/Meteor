package com.meteor.user;

import com.meteor.common.utils.PrintMeteor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户模块启动类
 *
 * @author Programmer
 * @date 2026-01-16 12:35
 */
@SpringBootApplication
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        PrintMeteor.printWelcomeBanner();
    }

}

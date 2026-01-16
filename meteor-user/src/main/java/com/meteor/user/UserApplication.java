package com.meteor.user;

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
        printWelcomeBanner();
    }

    private static void printWelcomeBanner() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        clearConsole();
        printAsciiArt();
    }

    private static void clearConsole() {
        // 打印空行清屏效果
        for (int i = 0; i < 60; i++) {
            System.out.println();
        }
    }

    private static void printAsciiArt() {
        String[] banner = {
                "",
                " " + "\u001B[36m" + "╔═════════════════════════════════════════════════════════════╗" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "                                                             " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "                        " +  "\u001B[1m" +  "\u001B[92m" + "Welcome Meteor" + "\u001B[0m" + "                       " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "                                                             " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "     " +  "\u001B[94m" + "███╗   ███╗███████╗████████╗███████╗ ██████╗ ██████╗ " + "\u001B[0m" + "   " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "     " +  "\u001B[94m" + "████╗ ████║██╔════╝╚══██╔══╝██╔════╝██╔═══██╗██╔══██╗" + "\u001B[0m" + "   " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "     " + "\u001B[96m" + "██╔████╔██║█████╗     ██║   █████╗  ██║   ██║██████╔╝" + "\u001B[0m" + "   " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "     " +  "\u001B[92m" + "██║╚██╔╝██║██╔══╝     ██║   ██╔══╝  ██║   ██║██╔══██╗" + "\u001B[0m" + "   " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "     " + "\u001B[93m" + "██║ ╚═╝ ██║███████╗   ██║   ███████╗╚██████╔╝██║  ██║" + "\u001B[0m" + "   " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "     " + "\u001B[95m" + "╚═╝     ╚═╝╚══════╝   ╚═╝   ╚══════╝ ╚═════╝ ╚═╝  ╚═╝" + "\u001B[0m" + "   " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "                                                             " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "║" + "\u001B[0m" + "                     " +  "\u001B[1m" + getCurrentDateTime()  + "\u001B[0m" + "                     " + "\u001B[36m" + "║" + "\u001B[0m",
                " " + "\u001B[36m" + "╚═════════════════════════════════════════════════════════════╝" + "\u001B[0m",
                "",
        };

        for (String line : banner) {
            System.out.println(line);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static String getCurrentDateTime() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

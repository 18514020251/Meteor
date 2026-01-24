package com.meteor.common.utils;

/**
 * 打印 Meteor 欢迎横幅
 *
 * @author Programmer
 * @date 2026-01-17 10:02
 */
@SuppressWarnings("squid:S106")
public class PrintMeteor {

    private PrintMeteor() {
    }

    private static final int WELCOME_BANNER_DELAY = 500;
    private static final int BANNER_LINE_DELAY = 50;
    private static final int CLEAR_CONSOLE_LINES = 60;

    public static void printWelcomeBanner() {
        try {
            Thread.sleep(WELCOME_BANNER_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        clearConsole();
        printAsciiArt();
    }

    private static void clearConsole() {
        // 打印空行清屏效果
        for (int i = 0; i < CLEAR_CONSOLE_LINES; i++) {
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
                Thread.sleep(BANNER_LINE_DELAY);
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

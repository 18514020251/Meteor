/**
 * @author Programmer
 * @date 2026-01-16 21:17
 */

import java.io.*;
import java.net.Socket;

public class TestRedisSocket {
    public static void main(String[] args) {
        System.out.println("=== Redis Socket原始连接测试 ===");

        String host = "localhost";
        int port = 6379;
        String password = "07210521";

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("✅ Socket连接建立");

            // 发送AUTH命令
            String authCommand = String.format("*2\r\n$4\r\nAUTH\r\n$%d\r\n%s\r\n",
                    password.length(), password);
            out.print(authCommand);
            out.flush();

            String authResponse = in.readLine();
            System.out.println("AUTH响应: " + authResponse);

            if (!authResponse.startsWith("+OK")) {
                throw new RuntimeException("认证失败: " + authResponse);
            }

            // 发送PING命令
            out.print("*1\r\n$4\r\nPING\r\n");
            out.flush();

            String pingResponse = in.readLine();
            System.out.println("PING响应: " + pingResponse);

            // 测试SET命令
            String key = "socket_test";
            String value = "socket_value_" + System.currentTimeMillis();
            String setCommand = String.format("*3\r\n$3\r\nSET\r\n$%d\r\n%s\r\n$%d\r\n%s\r\n",
                    key.length(), key, value.length(), value);
            out.print(setCommand);
            out.flush();

            String setResponse = in.readLine();
            System.out.println("SET响应: " + setResponse);

            // 测试GET命令
            String getCommand = String.format("*2\r\n$3\r\nGET\r\n$%d\r\n%s\r\n",
                    key.length(), key);
            out.print(getCommand);
            out.flush();

            // Redis协议解析
            String line1 = in.readLine(); // 第一行：$长度
            String line2 = in.readLine(); // 第二行：实际值
            System.out.println("GET响应: " + line1 + " -> " + line2);

            System.out.println("\n✅ Redis原始协议测试成功！");

        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.meteor.user.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "programmer.mysql.host=localhost",
        "programmer.mysql.username=root",
        "programmer.mysql.password=password",
        "programmer.redis.host=localhost",
        "programmer.redis.port=6379",
        "programmer.redis.password="
})
public class ConfigTest {

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username}")
    private String dataSourceUsername;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${programmer.mysql.host}")
    private String mysqlHost;

    @Value("${programmer.mysql.username}")
    private String mysqlUsername;

    @Value("${programmer.redis.host}")
    private String redisHost;

    @Value("${programmer.redis.port}")
    private Integer redisPort;

    @Test
    public void testConfigValues() {
        System.out.println("======= Configuration Values =======");
        System.out.println("Mysql Host: " + mysqlHost);
        System.out.println("Mysql Username: " + mysqlUsername);
        System.out.println("Datasource URL: " + dataSourceUrl);
        System.out.println("Datasource Username: " + dataSourceUsername);
        System.out.println("Driver Class Name: " + driverClassName);
        System.out.println("Redis Host: " + redisHost);
        System.out.println("Redis Port: " + redisPort);
        System.out.println("====================================");

        // 验证关键配置是否存在
        if (dataSourceUrl == null || dataSourceUrl.contains("${")) {
            System.err.println("ERROR: Datasource URL is not resolved properly!");
        } else {
            System.out.println("SUCCESS: Datasource URL is resolved properly.");
        }

        if (mysqlHost == null || mysqlHost.contains("${")) {
            System.err.println("ERROR: Mysql Host is not resolved properly!");
        } else {
            System.out.println("SUCCESS: Mysql Host is resolved properly.");
        }
    }
}

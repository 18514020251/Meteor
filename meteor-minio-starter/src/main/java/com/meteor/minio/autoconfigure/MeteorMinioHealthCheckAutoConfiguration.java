package com.meteor.minio.autoconfigure;

import com.meteor.minio.properties.MeteorMinioProperties;
import com.meteor.minio.startup.MinioConnectionChecker;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 *  Minio 启动连接测试
 *
 * @author Programmer
 * @date 2026-01-28 19:41
 */
@AutoConfiguration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MeteorMinioProperties.class)
@ConditionalOnProperty(prefix = "meteor.minio.health-check", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MeteorMinioHealthCheckAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MinioConnectionChecker.class)
    public MinioConnectionChecker minioConnectionChecker(MinioClient minioClient, MeteorMinioProperties props) {
        return new MinioConnectionChecker(minioClient, props);
    }
}

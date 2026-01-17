package com.meteor.minio.autoconfigure;

import com.meteor.minio.properties.MeteorMinioProperties;
import com.meteor.minio.util.MinioUtil;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Minio 自动配置
 *
 * @author Programmer
 * @date 2026-01-17 20:44
 */
@AutoConfiguration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MeteorMinioProperties.class)
public class MeteorMinioAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient(MeteorMinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(
                        properties.getAccessKey(),
                        properties.getSecretKey()
                )
                .build();
    }

    @Bean
    public MinioUtil minioUtil(
            MinioClient minioClient,
            MeteorMinioProperties properties
    ) {
        return new MinioUtil(minioClient, properties);
    }
}

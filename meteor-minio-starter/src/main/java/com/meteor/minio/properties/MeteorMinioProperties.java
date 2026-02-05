package com.meteor.minio.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置类
 *  获取配置信息
 *
 * @author Programmer
 * @date 2026-01-17 20:44
 */
@Data
@ConfigurationProperties(prefix = "meteor.minio")
public class MeteorMinioProperties {

    private static final int DEFAULT_EXPIRE_MINUTES = 60;

    /**
     * MinIO 服务地址
     */
    private String endpoint;

    /**
     * 访问 Key
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 默认 Bucket
     */
    @NotBlank(message = "默认 Bucket 不能为空")
    private String bucket;

    /*
    *  是否使用路径样式
    * */
    private boolean pathStyle = true;

    /*
    *  头像有效期
    * */
    private int presignedUrlExpireMinutes = DEFAULT_EXPIRE_MINUTES;

}

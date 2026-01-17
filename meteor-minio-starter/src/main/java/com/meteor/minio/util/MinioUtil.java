package com.meteor.minio.util;

import com.meteor.minio.properties.MeteorMinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * Minio 工具类
 *
 * @author Programmer
 * @date 2026-01-17 20:44
 */
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;
    private final MeteorMinioProperties properties;

    /**
     * 上传头像
     */
    public String uploadAvatar(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {

            String objectName = "avatar/"
                    + UUID.randomUUID()
                    + "-"
                    + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("上传头像失败", e);
        }
    }
}


package com.meteor.minio.util;

import com.meteor.common.exception.CommonErrorCode;
import com.meteor.minio.properties.MeteorMinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
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
     * 上传文件到 MinIO 指定路径
     *
     * <p>
     * 注意：
     * <ul>
     *   <li>path 表示对象在 MinIO 中的逻辑目录</li>
     *   <li>不包含 bucket 名称</li>
     *   <li>推荐通过 {@link com.meteor.minio.enums.MinioPathEnum}
     *       获取路径，避免硬编码字符串</li>
     * </ul>
     * </p>
     *
     * @param path  MinIO 对象存储路径（如 avatar、movie/poster）
     *              <br/>建议使用 {@code MinioPathEnum.path()} 获取
     * @param file  上传的文件
     * @return      对象在 MinIO 中的完整 objectName
     */
    public String upload(String path, MultipartFile file) {
        String objectName = path + "/"
                + UUID.randomUUID()
                + "-"
                + UUID.randomUUID();

        try (InputStream is = file.getInputStream()) {

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
            throw new RuntimeException(CommonErrorCode.FILE_UPLOAD_FAILED.getMessage(), e);
        }
    }

    /**
     * 删除 MinIO 上的文件
     *
     * @param objectName 文件的对象名称
     */
    public void delete(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(CommonErrorCode.FILE_DELETE_FAILED.getMessage(), e);
        }
    }

    /*
     *  构建用户头像 URL
     * */
    public String buildObjectUrl(String objectName) {
        if (properties.isPathStyle()) {
            return properties.getEndpoint()
                    + "/" + properties.getBucket()
                    + "/" + objectName;
        }
        return properties.getEndpoint()
                + "/" + objectName;
    }
}

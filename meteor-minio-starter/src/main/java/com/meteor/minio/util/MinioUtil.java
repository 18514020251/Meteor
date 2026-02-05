package com.meteor.minio.util;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.minio.properties.MeteorMinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.meteor.common.constants.MinioConstants.MIN_PART_SIZE;

/**
 * Minio 工具类
 *
 * @author Programmer
 * @date 2026-01-17 20:44
 */
@Slf4j
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
            throw new BizException(CommonErrorCode.FILE_UPLOAD_FAILED);
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
            throw new BizException(CommonErrorCode.FILE_DELETE_FAILED);
        }
    }


    /**
     * 生成对象的临时访问 URL（Presigned URL）
     *
     * @param objectName MinIO 中的对象名（UUID-UUID）
     * @return           带签名的临时访问 URL
     */
    public String buildPresignedUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .expiry(
                                    properties.getPresignedUrlExpireMinutes(),
                                    TimeUnit.MINUTES
                            )
                            .build()
            );
        } catch (Exception e) {
            throw new BizException(CommonErrorCode.FILE_URL_GENERATE_FAILED);
        }
    }

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
     * @param inputStream  上传的文件
     * @param contentType  文件类型
     * @return      对象在 MinIO 中的完整 objectName
     */
    public String upload(String path, InputStream inputStream, String contentType) {

        String objectName = path + "/"
                + UUID.randomUUID()
                + "-"
                + UUID.randomUUID();

        try {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(inputStream, -1, MIN_PART_SIZE)
                            .contentType(contentType)
                            .build()
            );

            return objectName;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new BizException(CommonErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public void putObject(
            String objectName,
            InputStream inputStream,
            String contentType
    ) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(inputStream, -1, MIN_PART_SIZE)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            log.error("上传文件失败，objectName={}", objectName, e);
            throw new BizException(CommonErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     *  构建公开访问 URL
     * */
    public String buildPublicUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }
        return properties.getEndpoint()
                + "/"
                + properties.getBucket()
                + "/"
                + objectName;
    }

}

package com.meteor.user.startup;

import com.meteor.common.constants.AvatarConstants;
import com.meteor.common.constants.MinioConstants;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 *  MinIO 默认头像初始化器
 *
 * @author Programmer
 * @date 2026-01-26 21:50
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioDefaultAvatarInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final String BUCKET = MinioConstants.BUCKET_METEOR;
    private static final String OBJECT = AvatarConstants.DEFAULT_AVATAR;
    private static final String CLASSPATH_FILE = MinioConstants.DEFAULT_AVATAR_CLASSPATH;

    private final MinioClient minioClient;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            ensureBucket();
            ensureDefaultAvatar();

            log.info("""
                    ======== MinIO Init (meteor-user) ========
                    Bucket : {}
                    Object : {}
                    Status : OK
                    =========================================
                    """, BUCKET, OBJECT);

        } catch (Exception e) {
            log.error("""
                    ======== MinIO Init (meteor-user) ========
                    Bucket : {}
                    Object : {}
                    Status : FAIL
                    Cause  : {}
                    =========================================
                    """, BUCKET, OBJECT, rootMessage(e));
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(MinioDefaultAvatarInitializer.BUCKET)
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(MinioDefaultAvatarInitializer.BUCKET)
                    .build());
            log.warn("MinIO bucket not found, created: {}", MinioDefaultAvatarInitializer.BUCKET);
        }
    }

    private void ensureDefaultAvatar() throws Exception {
        if (objectExists()) {
            return;
        }

        ClassPathResource resource = new ClassPathResource(MinioDefaultAvatarInitializer.CLASSPATH_FILE);
        if (!resource.exists()) {
            throw new IllegalStateException("Default avatar not found in classpath: " + MinioDefaultAvatarInitializer.CLASSPATH_FILE);
        }

        try (InputStream in = resource.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(MinioDefaultAvatarInitializer.BUCKET)
                    .object(MinioDefaultAvatarInitializer.OBJECT)
                    .stream(in, resource.contentLength(), -1)
                    .contentType("image/png")
                    .build());
        }

        log.warn("MinIO default avatar not found, uploaded: {}/{}", MinioDefaultAvatarInitializer.BUCKET, MinioDefaultAvatarInitializer.OBJECT);
    }

    private boolean objectExists() {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(MinioDefaultAvatarInitializer.BUCKET)
                    .object(MinioDefaultAvatarInitializer.OBJECT)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse() != null && "NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new BizException(CommonErrorCode.SYSTEM_ERROR,
                    "MinIO statObject failed: " + e.errorResponse().message());
        } catch (Exception e) {
            // 兜底 → 转业务异常
            throw new BizException(CommonErrorCode.SYSTEM_ERROR,
                    "MinIO connection error: " + e.getMessage());
        }
    }


    private String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur.getClass().getSimpleName() + ": " + cur.getMessage();
    }
}

package com.meteor.movie.startup;

import com.meteor.common.constants.MinioConstants;
import com.meteor.common.constants.PosterConstants;
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
 * MinIO 默认海报初始化器（meteor-movie）
 *
 * @author Programmer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioDefaultPosterInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final String BUCKET = MinioConstants.BUCKET_METEOR_MOVIE;
    private static final String OBJECT = PosterConstants.DEFAULT_POSTER;
    private static final String CLASSPATH_FILE = MinioConstants.DEFAULT_POSTER_CLASSPATH;

    private final MinioClient minioClient;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            ensureBucket();
            ensureDefaultPoster();

            log.info("""
                    ======== MinIO Init (meteor-movie) ========
                    Bucket : {}
                    Object : {}
                    Status : OK
                    ==========================================
                    """, BUCKET, OBJECT);

        } catch (Exception e) {
            log.error("""
                    ======== MinIO Init (meteor-movie) ========
                    Bucket : {}
                    Object : {}
                    Status : FAIL
                    Cause  : {}
                    ==========================================
                    """, BUCKET, OBJECT, rootMessage(e));
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(BUCKET).build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(BUCKET).build()
            );
            log.warn("MinIO bucket not found, created: {}", BUCKET);
        }
    }

    private void ensureDefaultPoster() throws Exception {
        if (objectExists()) {
            return;
        }

        ClassPathResource resource = new ClassPathResource(CLASSPATH_FILE);
        if (!resource.exists()) {
            throw new IllegalStateException("Default poster not found in classpath: " + CLASSPATH_FILE);
        }

        try (InputStream in = resource.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(OBJECT)
                            .stream(in, resource.contentLength(), -1)
                            .contentType("image/png")
                            .build()
            );
        }

        log.warn("MinIO default poster not found, uploaded: {}/{}", BUCKET, OBJECT);
    }

    private boolean objectExists() {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(OBJECT)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse() != null && "NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new BizException(CommonErrorCode.SYSTEM_ERROR,
                    "MinIO statObject failed: " + (e.errorResponse() == null ? e.getMessage() : e.errorResponse().message()));
        } catch (Exception e) {
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

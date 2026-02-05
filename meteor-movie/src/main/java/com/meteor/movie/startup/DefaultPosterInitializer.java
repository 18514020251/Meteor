package com.meteor.movie.startup;

import com.alibaba.nacos.common.packagescan.resource.ClassPathResource;
import com.meteor.common.constants.MinioConstants;
import com.meteor.common.constants.PosterConstants;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 *  默认海报初始化
 *
 * @author Programmer
 * @date 2026-02-04 20:59
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultPosterInitializer
        implements ApplicationListener<ApplicationReadyEvent> {

    private static final String BUCKET = MinioConstants.BUCKET_METEOR_MOVIE;

    private static final String OBJECT = PosterConstants.POSTER_PATH;

    private static final String CLASSPATH_FILE = MinioConstants.DEFAULT_POSTER;


    private final MinioClient minioClient;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ClassPathResource resource = new ClassPathResource(CLASSPATH_FILE);
        log.info("poster exists in classpath? {}", resource.exists());

        try {
            ensureBucket();
            ensurePoster();

            log.info("""
                    ======== MinIO Init (meteor-movie) ========
                    Poster : {}
                    Status : OK
                    ===========================================
                    """, OBJECT);

        } catch (Exception e) {
            log.error("MinIO poster init fail: {}", rootMessage(e));
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(BUCKET)
                        .build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(BUCKET)
                            .build()
            );
            log.warn("Bucket created: {}", BUCKET);
        }
    }

    private void ensurePoster() throws Exception {

        if (objectExists()) {
            log.info("Default poster already exists: {}/{}", BUCKET, OBJECT);
            return;
        }
        log.warn("Default poster not found, uploading: {}/{}", BUCKET, OBJECT);

        ClassPathResource resource =
                new ClassPathResource(CLASSPATH_FILE);

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

        log.warn("Default poster uploaded: {}/{}", BUCKET, OBJECT);
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
            return !"NoSuchKey".equals(e.errorResponse().code());
        } catch (Exception e) {
            return false;
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


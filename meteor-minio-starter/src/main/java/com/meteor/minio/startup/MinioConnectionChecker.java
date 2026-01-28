package com.meteor.minio.startup;

import com.meteor.minio.properties.MeteorMinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.Objects;

/**
 *  Minio 启动连接测试
 *
 * @author Programmer
 * @date 2026-01-28 19:42
 */
@Slf4j
@RequiredArgsConstructor
public class MinioConnectionChecker implements ApplicationListener<ApplicationReadyEvent> {

    private final MinioClient minioClient;
    private final MeteorMinioProperties props;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String endpoint = defaultStr(props.getEndpoint());
        String bucket = defaultStr(props.getBucket());

        try {
            // 轻量探活：列桶
            minioClient.listBuckets();

            log.info("""
                ======== Middleware Check (minio-starter) ========
                MinIO : OK
                Endpoint: {}
                Bucket  : {}
                ================================================
                """, endpoint, bucket);

        } catch (Exception e) {
            log.warn("""
                ======== Middleware Check (minio-starter) ========
                MinIO : FAIL
                Endpoint: {}
                Bucket  : {}
                ERR   : {}
                ================================================
                """, endpoint, bucket, rootMessage(e));
        }
    }

    private String defaultStr(String v) {
        return (v == null || v.isBlank()) ? "unknown" : v;
    }

    private String rootMessage(Throwable t) {
        Throwable cur = Objects.requireNonNull(t);
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur.getClass().getSimpleName() + ": " + cur.getMessage();
    }
}
package com.meteor.movie.job;

import com.meteor.minio.properties.MeteorMinioProperties;
import com.meteor.movie.mapper.MediaAssetMapper;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

/**
 * 媒体资源清理任务
 * 孤儿文件定义：
 * 1. 位于 movie/ 目录
 * 2. 创建时间早于 EXPIRE_HOURS
 * 3. media_asset 表中不存在（或 deleted=1）
 * @author Programmer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaCleanupJob {

    /** 超过多久还没被使用，就认为是孤儿文件 */
    private static final int EXPIRE_HOURS = 24;

    private final MinioClient minioClient;
    private final MeteorMinioProperties properties;
    private final MediaAssetMapper mediaAssetMapper;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredMediaFiles() {

        log.info("[MediaCleanupJob] start cleanup");

        try {
            Iterable<io.minio.Result<Item>> results =
                    minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(properties.getBucket())
                                    .prefix("movie/")
                                    .recursive(true)
                                    .build()
                    );

            for (io.minio.Result<Item> r : results) {
                Item item = r.get();

                ZonedDateTime lastModified = item.lastModified();
                String objectKey = item.objectName();

                boolean shouldSkip =
                        lastModified == null
                                || lastModified.isAfter(ZonedDateTime.now().minusHours(EXPIRE_HOURS))
                                || mediaAssetMapper.existsByObjectKey(objectKey) != null;

                if (shouldSkip) {
                    continue;
                }

                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(properties.getBucket())
                                .object(objectKey)
                                .build()
                );

                log.info("[MediaCleanupJob] deleted orphan media: {}", objectKey);
            }


        } catch (Exception e) {
            log.error("[MediaCleanupJob] cleanup failed", e);
        }

        log.info("[MediaCleanupJob] cleanup end");
    }
}

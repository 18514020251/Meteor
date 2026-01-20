package com.meteor.common.constants;

/**
 *  Minio 常量
 *
 * @author Programmer
 * @date 2026-01-20 15:50
 */
public final class MinioConstants {

    private MinioConstants() {}

    /** MinIO 分片上传最小 Part Size（5MB） */
    public static final long MIN_PART_SIZE = 5L * 1024 * 1024;
}

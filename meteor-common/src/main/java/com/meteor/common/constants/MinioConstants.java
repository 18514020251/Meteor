package com.meteor.common.constants;

/**
 *  Minio 常量
 *
 * @author Programmer
 * @date 2026-01-20 15:50
 */
public final class MinioConstants {

    private MinioConstants() {}

    public static final String BUCKET_METEOR = "meteor";

    /** 默认用户头像本地资源路径（classpath） */
    public static final String DEFAULT_AVATAR_CLASSPATH = "static/avatar/default.png";

    /** MinIO 分片上传最小 Part Size（5MB） */
    public static final long MIN_PART_SIZE = 5L * 1024 * 1024;

    public static final String BUCKET_METEOR_MOVIE = "meteor-movie";

    public static final String DEFAULT_POSTER_CLASSPATH  = "static/avatar/default.png";

    public static final String DEFAULT_POSTER = "static/default/movie-poster.jpg";

}

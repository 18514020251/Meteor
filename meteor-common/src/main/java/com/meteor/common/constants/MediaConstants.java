package com.meteor.common.constants;

import java.util.Set;

/**
 * @author Programmer
 * @date 2026-02-02 18:33
 */
public final class MediaConstants {

    private MediaConstants() {}

    /** 最大图片大小：5MB */
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024L;

    /** 允许的图片 MIME 类型 */
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp"
    );

    /** objectKey 前缀 */
    public static final String MOVIE_ROOT_DIR = "movie";
}

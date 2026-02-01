package com.meteor.common.constants;

import java.util.Set;

/**
 *  头像常量
 *
 * @author Programmer
 * @date 2026-01-18 11:32
 */
public final class AvatarConstants {

    private AvatarConstants() {}

    /** 默认用户头像 */
    public static final String DEFAULT_AVATAR = "avatar/default.png";

    /** 头像最大大小：5MB */
    // NOTE：本地环境可以5MB，项目上限或者简历时一定改了
    public static final long MAX_SIZE = 5L * 1024 * 1024;

    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_JPG = "image/jpg";

    public static final String FORMAT_PNG = "png";
    public static final String FORMAT_JPG = "jpg";

    /** 允许的 MIME 类型 */
    public static final Set<String> ALLOWED_TYPES = Set.of(
            IMAGE_JPEG,
            IMAGE_PNG,
            IMAGE_JPG
    );
}

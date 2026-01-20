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

    /** 头像最大大小：2MB */
    public static final long MAX_SIZE = 2 * 1024 * 1024;

    /** 允许的 MIME 类型 */
    public static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png"
    );
}

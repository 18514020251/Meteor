package com.meteor.minio.enums;

/**
 * MinIO 对象存储路径枚举
 *
 * <p>
 * 统一约束对象在 MinIO 中的存储目录，
 * 避免业务代码中硬编码字符串路径。
 * </p>
 *
 * <p>
 * 使用原则：
 * </p>
 * <ul>
 *   <li>对外：业务代码使用枚举</li>
 *   <li>对内：MinioUtil 仍使用 String</li>
 * </ul>
 *
 * @author Programmer
 */
public enum MinioPathEnum {


    USER_AVATAR("avatar"),


    MOVIE_POSTER("movie/poster");


    private final String path;

    MinioPathEnum(String path) {
        this.path = path;
    }


    public String path() {
        return path;
    }
}

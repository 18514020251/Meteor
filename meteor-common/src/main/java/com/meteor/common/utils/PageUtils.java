package com.meteor.common.utils;

/**
 *  分页工具类
 *
 * @author Programmer
 * @date 2026-01-25 11:01
 */
public class PageUtils {

    private PageUtils() {}

    public static int offset(int pageNum, int pageSize) {
        return Math.max(pageNum - 1, 0) * pageSize;
    }
}


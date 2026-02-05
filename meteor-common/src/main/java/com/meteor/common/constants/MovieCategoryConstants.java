package com.meteor.common.constants;

/**
 *  电影分类常量
 *
 * @author Programmer
 * @date 2026-02-03 21:24
 */
public class MovieCategoryConstants {
    private MovieCategoryConstants(){}

    public static final Integer MOVIE_CATEGORY_MIN = 1;
    public static final Integer MOVIE_CATEGORY_MAX = 15;

    /**
     *  主页最多电影数
     * */
    public static final Integer MAX_MOVIE_PER = 8;

    /**
     *  最新电影返回数
     * */
    public static final Integer LATEST_SIZE = 20;

    /**
     *  默认金额
     * */
    public static final Integer DEFAULT_PRICE = 0;

    /**
     *  默认热度
     * */
    public static final Integer DEFAULT_HOT_SCORE = 0;
}

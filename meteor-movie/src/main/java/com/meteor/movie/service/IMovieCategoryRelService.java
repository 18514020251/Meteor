package com.meteor.movie.service;

import com.meteor.movie.domain.entity.MovieCategoryRel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 电影分类关联表 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface IMovieCategoryRelService extends IService<MovieCategoryRel> {

    /**
     *  绑定分类
     *
     * @param movieId 电影ID
     * @param categoryIds 分类ID
     * @param operatorId 操作人ID
     * @param now 当前时间
     * */
    void bindCategories(Long movieId, List<Long> categoryIds, Long operatorId, LocalDateTime now);
}

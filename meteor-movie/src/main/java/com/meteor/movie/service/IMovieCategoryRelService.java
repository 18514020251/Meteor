package com.meteor.movie.service;

import com.meteor.movie.domain.entity.MovieCategoryRel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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


    /**
     * 按 quotaMap 从分类中挑选候选 movieId（去重后最多返回 size 个）
     *
     * @param quotaMap categoryId -> quota（例如 {14=5, 6=3}）
     * @param size 目标数量（固定8）
     * @return 电影id数组
     */
    List<Long> pickCandidateMovieIds(Map<Long, Integer> quotaMap, int size);
}

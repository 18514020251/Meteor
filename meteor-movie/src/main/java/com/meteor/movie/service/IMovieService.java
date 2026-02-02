package com.meteor.movie.service;

import com.meteor.movie.controller.dto.MovieCreateDTO;
import com.meteor.movie.controller.vo.MovieTitleVO;
import com.meteor.movie.domain.entity.Movie;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 电影信息表 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface IMovieService extends IService<Movie> {

    /**
     * 新增电影（含：分类关联 + 图片资源）
     *
     * @param dto 新增电影参数
     * @param operatorId 操作人ID
     * */
    void createMovie(@Valid MovieCreateDTO dto, Long operatorId);

    /**
     *  获取电影标题列表
     * */
    List<MovieTitleVO> getTitles(Long merchantId);
}

package com.meteor.movie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meteor.movie.controller.dto.MovieSearchDTO;
import com.meteor.movie.controller.vo.MovieCategoryVO;
import com.meteor.movie.controller.vo.MovieSearchVO;
import com.meteor.movie.domain.entity.MovieCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 电影分类表 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface IMovieCategoryService extends IService<MovieCategory> {

    List<MovieCategoryVO> listAll();

    Page<MovieSearchVO> search(MovieSearchDTO dto);
}

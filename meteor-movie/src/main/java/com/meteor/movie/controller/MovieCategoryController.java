package com.meteor.movie.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meteor.common.result.PageResult;
import com.meteor.common.result.Result;
import com.meteor.movie.controller.dto.MovieSearchDTO;
import com.meteor.movie.controller.vo.MovieCategoryVO;
import com.meteor.movie.controller.vo.MovieSearchVO;
import com.meteor.movie.service.IMovieCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 电影分类表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Tag(name = "电影分类表")
public class MovieCategoryController {

    private final IMovieCategoryService service;

    @GetMapping("/categories")
    @Operation(summary = "获取所有电影分类")
    public Result<List<MovieCategoryVO>> listCategories() {
        return Result.success(service.listAll());
    }


    /**
     * 电影库搜索：只查 movie 模块（不依赖票务/场次）
     */
    @Operation(summary = "电影库搜索")
    @GetMapping("/search")
    public Result<PageResult<MovieSearchVO>> search(MovieSearchDTO dto) {
        Page<MovieSearchVO> page = service.search(dto);
        PageResult<MovieSearchVO> pr = PageResult.of(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pr);
    }
}

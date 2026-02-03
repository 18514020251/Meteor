package com.meteor.movie.controller;


import com.meteor.common.result.Result;
import com.meteor.movie.controller.vo.MovieCategoryVO;
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
@RequestMapping("/movies/categories")
@RequiredArgsConstructor
@Tag(name = "电影分类表")
public class MovieCategoryController {

    private final IMovieCategoryService service;

    @GetMapping
    @Operation(summary = "获取所有电影分类")
    public Result<List<MovieCategoryVO>> listCategories() {
        return Result.success(service.listAll());
    }


}

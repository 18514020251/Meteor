package com.meteor.movie.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.meteor.common.result.Result;
import com.meteor.movie.controller.dto.MovieCreateDTO;
import com.meteor.movie.controller.vo.HomeMovieCardVO;
import com.meteor.movie.controller.vo.MovieTitleVO;
import com.meteor.movie.service.IMovieService;
import com.meteor.satoken.constants.RoleConst;
import com.meteor.satoken.context.LoginContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 电影信息表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Tag( name = "电影信息表" )
public class MovieController {

    private final IMovieService movieService;
    private final LoginContext loginContext;

    @SaCheckRole(RoleConst.MERCHANT)
    @PostMapping
    public Result<Void> create(@Valid @RequestBody MovieCreateDTO dto) {
        Long operatorId = loginContext.currentLoginId();
        movieService.createMovie(dto, operatorId);
        return Result.success();
    }


    @SaCheckRole(RoleConst.MERCHANT)
    @GetMapping("/titles")
    public Result<List<MovieTitleVO>> getMyMovieTitles() {
        Long merchantId = loginContext.currentLoginId();
        return Result.success(movieService.getTitles(merchantId));
    }

    @Operation(summary = "获取用户首页推荐列表")
    @GetMapping("/home")
    public Result<List<HomeMovieCardVO>> home() {
        Long uid = loginContext.currentLoginId();
        return Result.success(movieService.home(uid));
    }

}

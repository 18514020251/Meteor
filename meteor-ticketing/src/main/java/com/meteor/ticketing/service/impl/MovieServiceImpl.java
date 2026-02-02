package com.meteor.ticketing.service.impl;

import com.meteor.ticketing.domain.entity.Movie;
import com.meteor.ticketing.mapper.MovieMapper;
import com.meteor.ticketing.service.IMovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 电影信息表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements IMovieService {

}

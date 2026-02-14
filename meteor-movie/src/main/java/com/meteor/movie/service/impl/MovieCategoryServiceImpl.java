package com.meteor.movie.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meteor.api.enums.MediaAssetKindEnum;
import com.meteor.api.enums.MediaBizTypeEnum;
import com.meteor.common.constants.PageConstants;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.minio.util.MinioUtil;
import com.meteor.movie.controller.dto.MovieSearchDTO;
import com.meteor.movie.controller.vo.MovieCategoryVO;
import com.meteor.movie.controller.vo.MovieSearchVO;
import com.meteor.movie.domain.entity.MediaAsset;
import com.meteor.movie.domain.entity.Movie;
import com.meteor.movie.domain.entity.MovieCategory;
import com.meteor.movie.domain.entity.MovieCategoryRel;
import com.meteor.movie.mapper.MediaAssetMapper;
import com.meteor.movie.mapper.MovieCategoryMapper;
import com.meteor.movie.mapper.MovieCategoryRelMapper;
import com.meteor.movie.mapper.MovieMapper;
import com.meteor.movie.service.IMovieCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 电影分类表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
@RequiredArgsConstructor
public class MovieCategoryServiceImpl extends ServiceImpl<MovieCategoryMapper, MovieCategory> implements IMovieCategoryService {

    private final MovieMapper movieMapper;
    private final MediaAssetMapper mediaAssetMapper;
    private final MovieCategoryRelMapper movieCategoryRelMapper;
    private final MovieCategoryMapper movieCategoryMapper;
    private final MinioUtil minioUtil;

    @Override
    public List<MovieCategoryVO> listAll() {

        return lambdaQuery()
                .select(MovieCategory::getId, MovieCategory::getName)
                .eq(MovieCategory::getDeleted, DeleteStatus.NORMAL)
                .orderByAsc(MovieCategory::getSort)
                .list()
                .stream()
                .map(c -> new MovieCategoryVO(c.getId(), c.getName()))
                .toList();
    }



    /**
     * 电影库搜索：只查 movie 模块（不依赖票务/场次）
     */
    public Page<MovieSearchVO> search(MovieSearchDTO dto) {

        long current = (dto.getPage() == null || dto.getPage() <= 0) ? PageConstants.DEFAULT_PAGE_NUM : dto.getPage();
        long size = (dto.getSize() == null || dto.getSize() <= 0) ? PageConstants.ADMIN_FIXED_PAGE_SIZE : dto.getSize();

        Page<Movie> page = new Page<>(current, size);

        LambdaQueryWrapper<Movie> qw = new LambdaQueryWrapper<Movie>()
                .eq(Movie::getDeleted, DeleteStatus.NORMAL);

        if (dto.getStatus() != null) {
            qw.eq(Movie::getStatus, dto.getStatus());
        }

        if (dto.getReleaseFrom() != null) {
            qw.ge(Movie::getReleaseDate, dto.getReleaseFrom());
        }
        if (dto.getReleaseTo() != null) {
            qw.le(Movie::getReleaseDate, dto.getReleaseTo());
        }

        if (StringUtils.hasText(dto.getQ())) {
            String q = dto.getQ().trim();
            qw.and(w -> w.like(Movie::getTitle, q)
                    .or().like(Movie::getAlias, q)
                    .or().like(Movie::getIntro, q));
        }

        if (!CollectionUtils.isEmpty(dto.getCategoryIds())) {
            String in = dto.getCategoryIds().stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            if (StringUtils.hasText(in)) {
                qw.inSql(Movie::getId,
                        "SELECT movie_id FROM movie_category_rel WHERE deleted=0 AND category_id IN (" + in + ")");
            }
        }

        String sort = dto.getSort();
        if ("createTimeDesc".equalsIgnoreCase(sort)) {
            qw.orderByDesc(Movie::getCreateTime).orderByDesc(Movie::getId);
        } else {
            qw.orderByDesc(Movie::getReleaseDate).orderByDesc(Movie::getId);
        }

        movieMapper.selectPage(page, qw);

        List<Movie> movies = page.getRecords();
        if (CollectionUtils.isEmpty(movies)) {
            Page<MovieSearchVO> empty = new Page<>(current, size);
            empty.setTotal(page.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        List<Long> movieIds = movies.stream().map(Movie::getId).toList();

        Map<Long, String> posterMap = loadPosterMap(movieIds);

        Map<Long, List<String>> categoriesMap = loadCategoriesMap(movieIds);

        List<MovieSearchVO> voList = movies.stream()
                .map(m -> new MovieSearchVO(
                        String.valueOf(m.getId()),
                        m.getTitle(),
                        m.getAlias(),
                        m.getDurationMin(),
                        m.getReleaseDate(),
                        m.getStatus(),
                        posterMap.get(m.getId()),
                        categoriesMap.getOrDefault(m.getId(), List.of())
                ))
                .toList();

        Page<MovieSearchVO> out = new Page<>(page.getCurrent(), page.getSize());
        out.setTotal(page.getTotal());
        out.setRecords(voList);
        return out;
    }

    /**
     * 批量补 poster：
     */
    private Map<Long, String> loadPosterMap(List<Long> movieIds) {
        LambdaQueryWrapper<MediaAsset> qw = new LambdaQueryWrapper<MediaAsset>()
                .eq(MediaAsset::getDeleted, 0)
                .eq(MediaAsset::getBizType, MediaBizTypeEnum.MOVIE)
                .eq(MediaAsset::getKind, MediaAssetKindEnum.POSTER)
                .in(MediaAsset::getBizId, movieIds)
                .orderByAsc(MediaAsset::getBizId)
                .orderByAsc(MediaAsset::getSort)
                .orderByDesc(MediaAsset::getCreateTime)
                .orderByDesc(MediaAsset::getId);

        List<MediaAsset> list = mediaAssetMapper.selectList(qw);

        Map<Long, String> map = new HashMap<>();
        for (MediaAsset a : list) {
            String url = minioUtil.buildPublicUrl(a.getObjectKey());
            map.putIfAbsent(a.getBizId(), url);
        }
        return map;
    }

    private Map<Long, List<String>> loadCategoriesMap(List<Long> movieIds) {
        List<MovieCategoryRel> rels = movieCategoryRelMapper.selectList(
                new LambdaQueryWrapper<MovieCategoryRel>()
                        .eq(MovieCategoryRel::getDeleted, 0)
                        .in(MovieCategoryRel::getMovieId, movieIds)
        );
        if (CollectionUtils.isEmpty(rels)) {
            return Map.of();
        }

        Set<Long> categoryIds = rels.stream()
                .map(MovieCategoryRel::getCategoryId)
                .collect(Collectors.toSet());

        List<MovieCategory> categories = movieCategoryMapper.selectList(
                new LambdaQueryWrapper<MovieCategory>()
                        .eq(MovieCategory::getDeleted, 0)
                        .in(MovieCategory::getId, categoryIds)
                        .orderByAsc(MovieCategory::getSort)
                        .orderByAsc(MovieCategory::getId)
        );

        Map<Long, MovieCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(MovieCategory::getId, Function.identity(), (a, b) -> a));

        Map<Long, List<MovieCategoryRel>> movieToRels = rels.stream()
                .collect(Collectors.groupingBy(MovieCategoryRel::getMovieId));

        Map<Long, List<String>> out = new HashMap<>();
        for (Map.Entry<Long, List<MovieCategoryRel>> e : movieToRels.entrySet()) {
            Long movieId = e.getKey();
            List<String> names = e.getValue().stream()
                    .map(MovieCategoryRel::getCategoryId)
                    .map(categoryMap::get)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(MovieCategory::getSort).thenComparing(MovieCategory::getId))
                    .map(MovieCategory::getName)
                    .distinct()
                    .toList();
            out.put(movieId, names);
        }
        return out;
    }
}

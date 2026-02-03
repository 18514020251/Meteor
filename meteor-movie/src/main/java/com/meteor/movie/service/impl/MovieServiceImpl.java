package com.meteor.movie.service.impl;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.movie.constants.MovieMediaConstants;
import com.meteor.movie.controller.dto.MovieCreateDTO;
import com.meteor.movie.controller.vo.MovieTitleVO;
import com.meteor.movie.domain.entity.Movie;
import com.meteor.movie.mapper.MovieMapper;
import com.meteor.movie.service.IMediaAssetService;
import com.meteor.movie.service.IMovieCategoryRelService;
import com.meteor.movie.service.IMovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.movie.service.assembler.MovieAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 电影信息表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */

@Service
@RequiredArgsConstructor
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements IMovieService {

    private final IMovieCategoryRelService movieCategoryRelService;
    private final IMediaAssetService mediaAssetService;
    private final MovieAssembler assembler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMovie(MovieCreateDTO dto, Long operatorId) {

        validateAndNormalizeMedia(dto);

        LocalDateTime now = LocalDateTime.now();

        Movie movie = assembler.create(dto , operatorId, now);

        try {
            baseMapper.insert(movie);
        } catch (DuplicateKeyException e) {
            throw new BizException(CommonErrorCode.CONFLICT);
        } catch (Exception e) {
            throw new BizException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        Long movieId = movie.getId();

        movieCategoryRelService.bindCategories(movieId, dto.getCategoryIds(), operatorId, now);

        mediaAssetService.createForMovie(movieId, dto.getPosterKey(), dto.getCoverKey(), dto.getGalleryKeys(), operatorId, now);
    }


    private void validateAndNormalizeMedia(MovieCreateDTO dto) {

        String poster = trimToNull(dto.getPosterKey());
        String cover  = trimToNull(dto.getCoverKey());
        List<String> gallery = normalizeGallery(dto.getGalleryKeys());

        dto.setPosterKey(poster);
        dto.setCoverKey(cover);
        dto.setGalleryKeys(gallery);

        assertNoDuplicateMedia(poster, cover, gallery);
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * 图集标准化：
     * - null -> 空列表
     * - trim + 去空
     * - 去重（保持顺序）
     * - 校验最大数量
     */
    private List<String> normalizeGallery(List<String> galleryKeys) {

        if (galleryKeys == null || galleryKeys.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> dedup = new LinkedHashSet<>();
        for (String key : galleryKeys) {
            String k = trimToNull(key);
            if (k != null) {
                dedup.add(k);
            }
        }

        if (dedup.size() > MovieMediaConstants.MAX_GALLERY) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        return new ArrayList<>(dedup);
    }

    /**
     * 重复校验：
     */
    private void assertNoDuplicateMedia(String poster, String cover, List<String> gallery) {

        List<String> all = new ArrayList<>();
        addIfNotNull(all, poster);
        addIfNotNull(all, cover);
        if (gallery != null && !gallery.isEmpty()) {
            all.addAll(gallery);
        }

        int uniqueCount = new HashSet<>(all).size();
        if (uniqueCount != all.size()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }
    }

    private void addIfNotNull(List<String> list, String val) {
        if (val != null) {
            list.add(val);
        }
    }


    @Override
    public List<MovieTitleVO> getTitles(Long merchantId) {
        return lambdaQuery()
                .select(Movie::getId, Movie::getTitle)
                .eq(Movie::getCreateBy, merchantId)
                .list()
                .stream()
                .map(m -> new MovieTitleVO(m.getId(), m.getTitle()))
                .toList();
    }
}


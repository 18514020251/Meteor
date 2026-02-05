package com.meteor.movie.service.impl;

import com.meteor.movie.domain.entity.MovieCategoryRel;
import com.meteor.movie.mapper.MovieCategoryRelMapper;
import com.meteor.movie.service.IMovieCategoryRelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.movie.service.assembler.MovieCategoryRelAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 电影分类关联表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MovieCategoryRelServiceImpl extends ServiceImpl<MovieCategoryRelMapper, MovieCategoryRel> implements IMovieCategoryRelService {

    private final MovieCategoryRelAssembler assembler;

    /**
     * 每类多取倍数，避免跨分类重复导致不够8条
     */
    private static final int CANDIDATE_MULTIPLIER = 3;

    /**
     * 单分类最多取多少候选，防止未来分类里电影很多时一次拉太多
     */
    private static final int MAX_LIMIT_PER_CATEGORY = 30;

    @Override
    public void bindCategories(Long movieId, List<Long> categoryIds, Long operatorId, LocalDateTime now) {

        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        List<Long> distinctIds = categoryIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        for (Long categoryId : distinctIds) {
            MovieCategoryRel rel = assembler.create(movieId, categoryId, operatorId, now);
            baseMapper.insert(rel);
        }
    }

    @Override
    public List<Long> pickCandidateMovieIds(Map<Long, Integer> quotaMap, int size) {
        if (quotaMap == null || quotaMap.isEmpty() || size <= 0) {
            return List.of();
        }

        List<Long> result = new ArrayList<>(size);
        Set<Long> seen = new HashSet<>(size * 2);

        for (Map.Entry<Long, Integer> entry : quotaMap.entrySet()) {
            appendFromCategory(entry, size, result, seen);
            if (result.size() >= size) {
                return result;
            }
        }

        log.info("candidate movieIds not enough, size={}, got={}", size, result.size());
        return result;
    }

    private void appendFromCategory(
            Map.Entry<Long, Integer> entry,
            int size,
            List<Long> result,
            Set<Long> seen
    ) {
        Long categoryId = entry.getKey();
        Integer quota = entry.getValue();

        if (!isValidQuota(categoryId, quota) || result.size() >= size) {
            return;
        }

        int limit = computeLimit(quota);

        List<Long> ids = baseMapper.selectMovieIdsByCategoryId(categoryId, limit);
        appendUnique(ids, size, result, seen);
    }

    private boolean isValidQuota(Long categoryId, Integer quota) {
        return categoryId != null && quota != null && quota > 0;
    }

    private int computeLimit(int quota) {
        return Math.min(quota * CANDIDATE_MULTIPLIER, MAX_LIMIT_PER_CATEGORY);
    }

    private void appendUnique(
            List<Long> ids,
            int size,
            List<Long> result,
            Set<Long> seen
    ) {
        if (ids == null || ids.isEmpty() || result.size() >= size) {
            return;
        }

        for (Long movieId : ids) {
            if (movieId == null) {
                continue;
            }
            if (seen.add(movieId)) {
                result.add(movieId);
                if (result.size() >= size) {
                    return;
                }
            }
        }
    }

}


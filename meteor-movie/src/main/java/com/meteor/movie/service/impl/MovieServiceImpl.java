package com.meteor.movie.service.impl;

import com.meteor.common.constants.MovieCategoryConstants;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.minio.util.MinioUtil;
import com.meteor.movie.client.UserPreferenceClient;
import com.meteor.movie.constants.MovieMediaConstants;
import com.meteor.movie.controller.dto.MovieCreateDTO;
import com.meteor.movie.controller.dto.TicketingMovieInfoListDTO;
import com.meteor.movie.controller.vo.HomeMovieCardVO;
import com.meteor.movie.controller.vo.MovieTitleVO;
import com.meteor.movie.domain.entity.Movie;
import com.meteor.movie.mapper.MovieMapper;
import com.meteor.movie.service.IMediaAssetService;
import com.meteor.movie.service.IMovieCategoryRelService;
import com.meteor.movie.service.IMovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.movie.service.assembler.MovieAssembler;
import com.meteor.movie.service.support.QuotaAllocator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.meteor.common.constants.AvatarConstants.DEFAULT_POSTER;
import static com.meteor.common.constants.MovieCategoryConstants.*;


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
@Slf4j
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements IMovieService {

    private final IMovieCategoryRelService movieCategoryRelService;
    private final IMediaAssetService mediaAssetService;
    private final MovieAssembler assembler;
    private final UserPreferenceClient userPreferenceClient;
    private final MovieQueryServiceImpl movieQueryService;
    private final MovieQueryServiceImpl mediaAssetQueryService;
    private final TicketingQueryService ticketingQueryService;
    private final MinioUtil minioUtil;

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

        movieTypeJudge(dto.getCategoryIds());

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

    /**
     *  电影类型判断
     * */
    private void movieTypeJudge(List<Long> list) {
        for (Long cid : list) {
            if (cid == null || cid < MovieCategoryConstants.MOVIE_CATEGORY_MIN || cid > MovieCategoryConstants.MOVIE_CATEGORY_MAX) {
                throw new BizException(CommonErrorCode.PARAM_ERROR);
            }
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

    @Override
    public List<HomeMovieCardVO> home(Long uid) {
        var pref = userPreferenceClient.getUserPreference(uid);
        Map<Long,Integer> quotaMap =
                QuotaAllocator.allocate(pref.getItems());
        List<Long> movieIds = movieCategoryRelService.pickCandidateMovieIds(quotaMap, MAX_MOVIE_PER);
        Map<Long, String> titleMap = movieQueryService.getTitleMap(movieIds);


        Map<Long, String> posterMap = mediaAssetQueryService.getPosterObjectKeyMap(movieIds)
                .entrySet()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> minioUtil.buildPublicUrl(e.getValue())
                ));

        Map<Long, List<String>> categoriesMap = mediaAssetQueryService.getCategoryNamesMap(movieIds);

        List<HomeMovieCardVO> cards = buildBaseCards(movieIds, titleMap, posterMap, categoriesMap);

        Map<Long, TicketingMovieInfoListDTO.Item> ticketingMap =
                ticketingQueryService.getInfoMap(movieIds);

        return cards.stream().map(c -> {
            var t = ticketingMap.get(c.movieId());
            if (t == null) {
                return normalize(c);
            }

            return normalize(new HomeMovieCardVO(
                    c.movieId(),
                    c.title(),
                    c.posterUrl(),
                    c.categories(),
                    t.getPrice() != null ? t.getPrice() : c.price(),
                    t.getInGrabPeriod() != null ? t.getInGrabPeriod() : c.inGrabPeriod(),
                    t.getHotScore() != null ? t.getHotScore() : c.hotScore()
            ));
        }).toList();

    }

    private HomeMovieCardVO normalize(HomeMovieCardVO vo) {
        Integer price = vo.price();
        Integer hotScore = vo.hotScore();

        if (price != null && price.equals(DEFAULT_PRICE)) {
            price = null;
        }
        if (hotScore != null && hotScore.equals(DEFAULT_HOT_SCORE)) {
            hotScore = null;
        }

        return new HomeMovieCardVO(
                vo.movieId(),
                vo.title(),
                vo.posterUrl(),
                vo.categories(),
                price,
                vo.inGrabPeriod(),
                hotScore
        );
    }



    private List<HomeMovieCardVO> buildBaseCards(
            List<Long> movieIds,
            Map<Long, String> titleMap,
            Map<Long, String> posterMap,
            Map<Long, List<String>> categoriesMap
    ) {


        List<HomeMovieCardVO> list = new ArrayList<>(movieIds.size());
        for (Long movieId : movieIds) {

            String title = titleMap.get(movieId);
            String poster = posterMap.getOrDefault(movieId, DEFAULT_POSTER);
            List<String> categories = categoriesMap.getOrDefault(movieId, List.of());

            list.add(new HomeMovieCardVO(
                    movieId,
                    title,
                    poster,
                    categories,
                    DEFAULT_PRICE,
                    false,
                    DEFAULT_HOT_SCORE
            ));
        }
        return list;
    }

}


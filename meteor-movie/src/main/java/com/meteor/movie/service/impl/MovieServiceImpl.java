package com.meteor.movie.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.meteor.api.contract.ticketing.dto.TicketingMovieInfoListDTO;
import com.meteor.common.constants.MovieCategoryConstants;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.minio.util.MinioUtil;
import com.meteor.api.contract.user.client.UserPreferenceClient;
import com.meteor.movie.constants.MovieMediaConstants;
import com.meteor.movie.controller.dto.MovieCreateDTO;
import com.meteor.movie.controller.vo.HomeMovieCardVO;
import com.meteor.movie.controller.vo.MovieTitleVO;
import com.meteor.movie.domain.entity.Movie;
import com.meteor.movie.enums.MovieStatusEnum;
import com.meteor.movie.mapper.MediaAssetMapper;
import com.meteor.movie.mapper.MovieCategoryRelMapper;
import com.meteor.movie.mapper.MovieMapper;
import com.meteor.movie.mapper.row.MoviePosterRow;
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
import java.util.stream.Collectors;

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
    private final MediaAssetMapper mediaAssetMapper;
    private final MovieCategoryRelMapper movieCategoryRelMapper;

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

    /**
     * 首页聚合核心链路（稳定版本）
     * 当前行为依赖多模块返回结构，请勿随意调整流程顺序。
     * 如需优化，仅允许在子方法内部重构。
     */
    @Override
    public List<HomeMovieCardVO> home(Long uid) {
        var pref = userPreferenceClient.getUserPreference(uid);

        Map<Long, Integer> quotaMap = QuotaAllocator.allocate(pref.getItems());
        List<Long> movieIds = movieCategoryRelService.pickCandidateMovieIds(quotaMap, MAX_MOVIE_PER);

        Map<Long, String> titleMap = movieQueryService.getTitleMap(movieIds);

        Map<Long, String> posterMap = mediaAssetQueryService.getPosterObjectKeyMap(movieIds)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> minioUtil.buildPublicUrl(e.getValue())
                ));

        Map<Long, List<String>> categoriesMap = mediaAssetQueryService.getCategoryNamesMap(movieIds);

        List<HomeMovieCardVO> cards = buildBaseCards(movieIds, titleMap, posterMap, categoriesMap);

        // ✅ 先拿 Long-key，再转 String-key（避免到处 parseLong）
        Map<String, TicketingMovieInfoListDTO.Item> ticketingMap =
                toStringKeyMap(ticketingQueryService.getInfoMap(movieIds));

        List<HomeMovieCardVO> main = cards.stream()
                .map(c -> buildHomeCardOrNull(c, ticketingMap))
                .flatMap(Optional::stream)
                .toList();

        if (main.size() >= MAX_MOVIE_PER) {
            return main.subList(0, MAX_MOVIE_PER);
        }

        Set<String> exists = main.stream()
                .map(HomeMovieCardVO::movieId)
                .collect(Collectors.toSet());

        List<HomeMovieCardVO> latest = latest20();

        List<HomeMovieCardVO> merged = new ArrayList<>(MAX_MOVIE_PER);
        merged.addAll(main);

        int need = MAX_MOVIE_PER - merged.size();
        if (need <= 0) {
            return merged;
        }

        List<HomeMovieCardVO> fills = latest.stream()
                .filter(Objects::nonNull)
                .filter(x -> exists.add(x.movieId()))
                .limit(need)
                .toList();

        merged.addAll(fills);
        return merged;
    }

    /**
     * Map<Long, Item> -> Map<String, Item>
     */
    private Map<String, TicketingMovieInfoListDTO.Item> toStringKeyMap(
            Map<Long, TicketingMovieInfoListDTO.Item> longKeyMap
    ) {
        if (longKeyMap == null || longKeyMap.isEmpty()) {
            return Map.of();
        }

        Map<String, TicketingMovieInfoListDTO.Item> map = new HashMap<>(longKeyMap.size() * 2);
        for (var e : longKeyMap.entrySet()) {
            Long k = e.getKey();
            TicketingMovieInfoListDTO.Item v = e.getValue();
            if (k == null || v == null) {
                continue;
            }
            map.put(String.valueOf(k), v);
        }
        return map;
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
                    String.valueOf(movieId),
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

    /**
     * 最新电影（按上映日期，固定 20 条），并远程补齐票务字段
     */
    @Override
    public List<HomeMovieCardVO> latest20() {

        List<Movie> movies = queryLatestMovies();
        if (movies == null || movies.isEmpty()) {
            return List.of();
        }

        List<Long> movieIds = movies.stream()
                .map(Movie::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, String> posterUrlMap = queryPosterUrlMap(movieIds);
        Map<Long, List<String>> categoryMap = queryCategoryMap(movieIds);

        Map<String, TicketingMovieInfoListDTO.Item> ticketingMap =
                toStringKeyMap(ticketingQueryService.getInfoMap(movieIds));

        return movies.stream()
                .map(m -> buildHomeCardOrNull(m, posterUrlMap, categoryMap, ticketingMap))
                .flatMap(Optional::stream)
                .toList();
    }


    private Optional<HomeMovieCardVO> buildHomeCardOrNull(
            HomeMovieCardVO base,
            Map<String, TicketingMovieInfoListDTO.Item> ticketingMap
    ) {
        String id = base.movieId();
        TicketingMovieInfoListDTO.Item t = ticketingMap.get(id);

        Integer price = t == null ? base.price() : defaultIfNull(t.getPrice(), base.price());
        Boolean inGrab = t == null ? base.inGrabPeriod() : defaultIfNull(t.getInGrabPeriod(), base.inGrabPeriod());
        Integer hotScore = t == null ? base.hotScore() : defaultIfNull(t.getHotScore(), base.hotScore());

        if (bothZero(price, hotScore)) {
            return Optional.empty();
        }

        return Optional.of(new HomeMovieCardVO(
                id,
                base.title(),
                base.posterUrl(),
                base.categories(),
                zeroToNull(price),
                Boolean.TRUE.equals(inGrab),
                zeroToNull(hotScore)
        ));
    }


    private Optional<HomeMovieCardVO> buildHomeCardOrNull(
            Movie m,
            Map<Long, String> posterUrlMap,
            Map<Long, List<String>> categoryMap,
            Map<String, TicketingMovieInfoListDTO.Item> ticketingMap
    ) {
        Long idLong = m.getId();
        if (idLong == null) {
            return Optional.empty();
        }

        String id = String.valueOf(idLong);

        HomeMovieCardVO base = new HomeMovieCardVO(
                id,
                m.getTitle(),
                posterUrlMap.getOrDefault(idLong, ""),
                categoryMap.getOrDefault(idLong, List.of()),
                DEFAULT_PRICE,
                false,
                DEFAULT_HOT_SCORE
        );

        return buildHomeCardOrNull(base, ticketingMap);
    }



    private boolean bothZero(Integer price, Integer hotScore) {
        return isZero(price) && isZero(hotScore);
    }

    private boolean isZero(Integer v) {
        return v == null || v == 0;
    }

    private Integer zeroToNull(Integer v) {
        return isZero(v) ? null : v;
    }

    private static <T> T defaultIfNull(T val, T defaultVal) {
        return val != null ? val : defaultVal;
    }


    /**
     * 查询最新电影（按上映日期）
     */
    private List<Movie> queryLatestMovies() {
        return baseMapper.selectList(
                Wrappers.<Movie>lambdaQuery()
                        .eq(Movie::getDeleted, DeleteStatus.NORMAL)
                        .in(Movie::getStatus, List.of(MovieStatusEnum.COMING, MovieStatusEnum.SHOWING))
                        .orderByDesc(Movie::getReleaseDate)
                        .orderByDesc(Movie::getId)
                        .last("LIMIT " + LATEST_SIZE)
        );
    }

    /**
     * movieId -> posterUrl（每个 movie 取 sort 最小的一张）
     */
    private Map<Long, String> queryPosterUrlMap(List<Long> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) {
            return Map.of();
        }

        List<MoviePosterRow> posters = mediaAssetMapper.selectPosterObjectKeyByMovieIds(movieIds);

        Map<Long, String> map = new HashMap<>(movieIds.size() * 2);
        for (MoviePosterRow row : posters) {
            if (row == null || row.movieId() == null) {
                continue;
            }
            map.putIfAbsent(row.movieId(), minioUtil.buildPublicUrl(row.objectKey()));
        }
        return map;
    }

    /**
     * movieId -> categories
     */
    private Map<Long, List<String>> queryCategoryMap(List<Long> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) {
            return Map.of();
        }


        var rows = movieCategoryRelMapper.listMovieCategoriesByMovieIds(movieIds);

        Map<Long, List<String>> map = new HashMap<>(movieIds.size() * 2);
        for (var row : rows) {
            if (row == null || row.getMovieId() == null) {
                continue;
            }
            map.computeIfAbsent(row.getMovieId(), k -> new ArrayList<>()).add(row.getCategoryName());
        }
        return map;
    }
}


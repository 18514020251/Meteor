package com.meteor.ticketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.api.contract.ticketing.dto.TicketingMovieInfoListDTO;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.ticketing.controller.dto.screening.ScreeningCreateDTO;
import com.meteor.ticketing.controller.enums.ScreeningStatusEnum;
import com.meteor.ticketing.domain.entity.Screening;
import com.meteor.ticketing.mapper.ScreeningMapper;
import com.meteor.ticketing.service.IScreeningService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.ticketing.service.assembler.ScreeningAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.meteor.common.constants.MovieCategoryConstants.DEFAULT_HOT_SCORE;

/**
 * <p>
 * 电影场次表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
@RequiredArgsConstructor
public class ScreeningServiceImpl extends ServiceImpl<ScreeningMapper, Screening> implements IScreeningService {

    private final HotRankServiceImpl hotRankService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long uid, ScreeningCreateDTO dto) {

        validateCreate(dto);

        LocalDateTime now = LocalDateTime.now();
        ScreeningStatusEnum initStatus = calcInitStatus(dto.getSaleStartTime(), dto.getSaleEndTime(), now);

        Screening screening = ScreeningAssembler.toNewEntity(uid, dto, initStatus, now);

        int rows = baseMapper.insert(screening);
        if (rows != 1) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "新增场次失败");
        }
    }


    private void validateCreate(ScreeningCreateDTO dto) {

        LocalDateTime start = dto.getStartTime();
        LocalDateTime end = dto.getEndTime();
        LocalDateTime saleStart = dto.getSaleStartTime();
        LocalDateTime saleEnd = dto.getSaleEndTime();

        if (saleStart.isAfter(start)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "开售时间不能晚于开场时间");
        }

        if (end != null && !end.isAfter(start)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "散场时间必须晚于开场时间");
        }

        if (saleEnd != null && saleEnd.isBefore(saleStart)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "停售时间不能早于开售时间");
        }

        if (saleEnd != null && saleEnd.isAfter(start)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "停售时间不能晚于开场时间");
        }
    }

    private ScreeningStatusEnum calcInitStatus(
            LocalDateTime saleStart,
            LocalDateTime saleEnd,
            LocalDateTime now) {

        if (now.isBefore(saleStart)) {
            return ScreeningStatusEnum.SCHEDULED;
        }

        if (saleEnd != null && now.isAfter(saleEnd)) {
            return ScreeningStatusEnum.CLOSED;
        }

        return ScreeningStatusEnum.SELLING;
    }

    @Override
    public TicketingMovieInfoListDTO batchGetMovieInfo(List<Long> movieIds) {

        TicketingMovieInfoListDTO dto = new TicketingMovieInfoListDTO();

        List<Long> ids = normalizeMovieIds(movieIds);
        if (ids.isEmpty()) {
            dto.setItems(List.of());
            return dto;
        }

        LocalDateTime now = LocalDateTime.now();

        List<Screening> screenings = queryScreenings(ids);
        if (screenings.isEmpty()) {
            dto.setItems(List.of());
            return dto;
        }

        Map<Long, List<Screening>> group = groupByMovieId(screenings);

        Chosen chosen = chooseForMovies(ids, group, now);
        if (chosen.chosenByMovie().isEmpty()) {
            dto.setItems(List.of());
            return dto;
        }

        Map<Long, Integer> hotScoreByScreeningId = queryHotScores(chosen.chosenScreeningIds());

        List<TicketingMovieInfoListDTO.Item> items =
                buildItems(ids, chosen.chosenByMovie(), hotScoreByScreeningId, now);

        dto.setItems(items);
        return dto;
    }

    private List<Long> normalizeMovieIds(List<Long> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) {
            return List.of();
        }
        return movieIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Screening> queryScreenings(List<Long> ids) {
        List<Screening> list = this.list(
                new LambdaQueryWrapper<Screening>()
                        .in(Screening::getMovieId, ids)
                        .eq(Screening::getDeleted, DeleteStatus.NORMAL.getCode())
        );
        return list == null ? List.of() : list;
    }

    private Map<Long, List<Screening>> groupByMovieId(List<Screening> screenings) {
        return screenings.stream().collect(Collectors.groupingBy(Screening::getMovieId));
    }

    private record Chosen(Map<Long, Screening> chosenByMovie, List<Long> chosenScreeningIds) {}

    private Chosen chooseForMovies(
            List<Long> ids,
            Map<Long, List<Screening>> group,
            LocalDateTime now
    ) {
        Map<Long, Screening> chosenByMovie = new HashMap<>((int) (ids.size() / 0.75f) + 1);
        List<Long> chosenScreeningIds = new ArrayList<>(ids.size());

        for (Long movieId : ids) {
            Screening chosen = chooseOne(group.get(movieId), now);
            if (chosen == null || chosen.getId() == null) {
                continue;
            }
            chosenByMovie.put(movieId, chosen);
            chosenScreeningIds.add(chosen.getId());
        }

        // 去重，避免重复查
        List<Long> uniq = chosenScreeningIds.stream().distinct().toList();
        return new Chosen(chosenByMovie, uniq);
    }

    private Screening chooseOne(List<Screening> list, LocalDateTime now) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return chooseScreening(list, now);
    }

    private Map<Long, Integer> queryHotScores(List<Long> screeningIds) {
        if (screeningIds == null || screeningIds.isEmpty()) {
            return Map.of();
        }
        return hotRankService.getScoreMap(screeningIds);
    }

    private List<TicketingMovieInfoListDTO.Item> buildItems(
            List<Long> ids,
            Map<Long, Screening> chosenByMovie,
            Map<Long, Integer> hotScoreByScreeningId,
            LocalDateTime now
    ) {
        List<TicketingMovieInfoListDTO.Item> items = new ArrayList<>(chosenByMovie.size());

        for (Long movieId : ids) {
            Screening chosen = chosenByMovie.get(movieId);
            if (chosen == null) {
                continue;
            }
            items.add(toItem(movieId, chosen, hotScoreByScreeningId, now));
        }
        return items;
    }

    private TicketingMovieInfoListDTO.Item toItem(
            Long movieId,
            Screening chosen,
            Map<Long, Integer> hotScoreByScreeningId,
            LocalDateTime now
    ) {
        TicketingMovieInfoListDTO.Item item = new TicketingMovieInfoListDTO.Item();
        item.setMovieId(movieId);
        item.setPrice(chosen.getMinPrice());
        item.setInGrabPeriod(isInGrabPeriod(chosen, now));

        Integer hotScore = hotScoreByScreeningId.get(chosen.getId());
        item.setHotScore(hotScore == null ? DEFAULT_HOT_SCORE : hotScore);

        return item;
    }


    private Screening chooseScreening(List<Screening> list, LocalDateTime now) {

        List<Screening> selling = list.stream()
                .filter(s -> isInGrabPeriod(s, now))
                .toList();

        if (!selling.isEmpty()) {
            return selling.stream()
                    .min(Comparator
                            .comparing(Screening::getMinPrice)
                            .thenComparing(Screening::getSaleEndTime, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(Screening::getId, Comparator.nullsLast(Comparator.reverseOrder()))
                    )
                    .orElse(null);
        }

        Optional<Screening> upcoming = list.stream()
                .filter(s -> s.getSaleStartTime() != null && s.getSaleStartTime().isAfter(now))
                .min(Comparator
                        .comparing(Screening::getSaleStartTime)
                        .thenComparing(Screening::getMinPrice)
                        .thenComparing(Screening::getId, Comparator.nullsLast(Comparator.reverseOrder()))
                );

        return upcoming.orElseGet(() -> list.stream()
                .filter(s -> s.getSaleEndTime() != null && s.getSaleEndTime().isBefore(now))
                .max(Comparator
                        .comparing(Screening::getSaleEndTime)
                        .thenComparing(Screening::getId, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .orElse(null));



    }

    private boolean isInGrabPeriod(Screening s, LocalDateTime now) {
        if (s == null) {
            return false;
        }

        LocalDateTime start = s.getSaleStartTime();
        if (start == null) {
            return false;
        }

        LocalDateTime end = s.getSaleEndTime();

        if (now.isBefore(start)) {
            return false;
        }

        if (end == null) {
            return true;
        }

        return !now.isAfter(end);
    }
}
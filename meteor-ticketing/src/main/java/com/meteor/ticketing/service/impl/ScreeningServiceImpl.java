package com.meteor.ticketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.meteor.api.contract.ticketing.dto.TicketingMovieInfoListDTO;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.ticketing.controller.dto.ScreeningOrderSnapshot;
import com.meteor.ticketing.controller.dto.screening.ScreeningCreateDTO;
import com.meteor.api.enums.ScreeningStatusEnum;
import com.meteor.ticketing.controller.vo.MovieScreeningVO;
import com.meteor.ticketing.controller.vo.ScreeningVO;
import com.meteor.ticketing.domain.entity.Screening;
import com.meteor.api.enums.SaleStateEnum;
import com.meteor.ticketing.mapper.ScreeningMapper;
import com.meteor.ticketing.redis.ScreeningConstants;
import com.meteor.ticketing.service.IScreeningService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.ticketing.service.assembler.ScreeningAssembler;
import com.meteor.ticketing.service.hot.ScreeningHotCounter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
@Slf4j
public class ScreeningServiceImpl extends ServiceImpl<ScreeningMapper, Screening> implements IScreeningService {

    private final HotRankServiceImpl hotRankService;
    private final ScreeningHotCounter screeningHotCounter;

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

        List<Long> uniq = chosenScreeningIds.stream().distinct().toList();
        return new Chosen(chosenByMovie, uniq);
    }

    private Screening chooseOne(List<Screening> list, LocalDateTime now) {
        if (list == null || list.isEmpty()) return null;

        return list.stream()
                .filter(s -> s != null && s.getId() != null)
                // NOTE(dev): 当前项目开发期允许展示历史场次（本地初始化数据多为过去时间）
                // 生产/商家数据接入后如需“只显示未开场/可售”，再恢复为 startTime >= now 的过滤口径
                //.filter(s -> s.getStartTime() != null && !now.isAfter(s.getStartTime()))
                .min((a, b) -> {
                    SaleStateEnum sa = calculateSaleState(a, now);
                    SaleStateEnum sb = calculateSaleState(b, now);

                    int pa = salePriority(sa);
                    int pb = salePriority(sb);
                    if (pa != pb) return Integer.compare(pa, pb);

                    if (sa == SaleStateEnum.NOT_STARTED) {
                        return nullSafeCompare(a.getSaleStartTime(), b.getSaleStartTime());
                    }
                    return nullSafeCompare(a.getStartTime(), b.getStartTime());
                })
                .orElse(null);
    }

    private int salePriority(SaleStateEnum s) {
        return switch (s) {
            case SELLING -> 1;
            case NOT_STARTED -> 2;
            case SOLD_OUT -> 3;
            case STOPPED -> 4;
            case CLOSED -> 5;
            case CANCELED -> 6;
        };
    }

    private int nullSafeCompare(LocalDateTime a, LocalDateTime b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
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

        SaleStateEnum state = calculateSaleState(chosen, now);
        item.setSaleState(state);

        Integer hotScore = hotScoreByScreeningId.get(chosen.getId());
        item.setHotScore(hotScore == null ? DEFAULT_HOT_SCORE : hotScore);

        return item;
    }


    @Override
    public List<MovieScreeningVO> getScreeningsByMovieId(Long movieId) {

        List<Screening> screenings = baseMapper.selectList(
                buildQueryWrapper(movieId)
        );

        if (CollectionUtils.isEmpty(screenings)) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        return screenings.stream()
                .map(screening -> buildMovieScreeningVO(screening, now))
                .toList();
    }

    private MovieScreeningVO buildMovieScreeningVO(Screening s, LocalDateTime now) {

        SaleStateEnum saleState = calculateSaleState(s, now);
        long remainSeconds = calculateRemainSeconds(now, saleState, s.getSaleStartTime());

        return new MovieScreeningVO(
                s.getId().toString(),
                s.getStartTime(),
                s.getEndTime(),

                s.getSaleStartTime(),
                s.getSaleEndTime(),

                s.getSaleMode(),

                s.getBasePrice(),
                s.getMinPrice(),
                s.getMaxPrice(),

                s.getTotalTickets(),
                s.getAvailableTickets(),
                s.getSoldTickets(),

                saleState,
                remainSeconds,

                System.currentTimeMillis()
        );
    }

    private long calculateRemainSeconds(LocalDateTime now,
                                        SaleStateEnum saleState,
                                        LocalDateTime saleStartTime) {

        if (saleState != SaleStateEnum.NOT_STARTED) {
            return 0L;
        }

        long seconds = Duration.between(now, saleStartTime).getSeconds();
        return Math.max(seconds, 0);
    }


    /**
     *  构建查询条件
     * */
    private LambdaQueryWrapper<Screening> buildQueryWrapper(Long movieId) {
        return Wrappers.<Screening>lambdaQuery()
                .eq(Screening::getMovieId, movieId)
                .eq(Screening::getDeleted, DeleteStatus.NORMAL)
                .ge(Screening::getStartTime, LocalDateTime.now())
                .orderByAsc(Screening::getStartTime);
    }


    @Override
    public ScreeningVO getRealtimeScreening(String screeningId) {
        Screening s = baseMapper.selectById(screeningId);
        if (s == null || DeleteStatus.DELETED.equals(s.getDeleted())) {
            throw new BizException(CommonErrorCode.SCREENING_NOT_FOUND);
        }

        long nowMs = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        long remainSeconds = 0L;
        LocalDateTime saleStart = s.getSaleStartTime();
        if (saleStart != null) {
            long diff = Duration.between(now, saleStart).getSeconds();
            remainSeconds = Math.max(diff, 0L);
        }

        SaleStateEnum saleState = calculateSaleState(s, now);

        return new ScreeningVO(
                String.valueOf(s.getId()),
                s.getStartTime(),
                s.getEndTime(),
                s.getSaleStartTime(),
                s.getSaleEndTime(),
                s.getMinPrice(),
                s.getMaxPrice(),
                s.getTotalTickets(),
                s.getAvailableTickets(),
                s.getSoldTickets(),
                saleState.name(),
                remainSeconds,
                nowMs
        );
    }

    private SaleStateEnum calculateSaleState(Screening s, LocalDateTime now) {

        LocalDateTime ss = s.getSaleStartTime();
        LocalDateTime se = s.getSaleEndTime();

        if (ss != null && se != null && se.isBefore(ss)) {
            se = null;
        }

        if (ScreeningStatusEnum.CANCELED.equals(s.getStatus())) {
            return SaleStateEnum.CANCELED;
        }

        if (ScreeningStatusEnum.CLOSED.equals(s.getStatus())) {
            return SaleStateEnum.CLOSED;
        }

        if (s.getStartTime() != null && now.isAfter(s.getStartTime())) {
            return SaleStateEnum.CLOSED;
        }

        if (se != null && now.isAfter(se)) {
            return SaleStateEnum.STOPPED;
        }

        if (ss != null && now.isBefore(ss)) {
            return SaleStateEnum.NOT_STARTED;
        }

        return SaleStateEnum.SELLING;
    }


    @Override
    public boolean decrStockAndIncrSold(Long screeningId) {
        return lambdaUpdate()
                .setSql("available_tickets = available_tickets - 1")
                .setSql("sold_tickets = sold_tickets + 1")
                .setSql("version = version + 1")
                .set(Screening::getUpdateTime, LocalDateTime.now())
                .eq(Screening::getId, screeningId)
                .eq(Screening::getDeleted, DeleteStatus.NORMAL)
                .gt(Screening::getAvailableTickets, ScreeningConstants.STOCK_EMPTY)
                .update();
    }

    @Override
    public void markSoldOutIfNeeded(Long screeningId) {
        lambdaUpdate()
                .set(Screening::getStatus, ScreeningStatusEnum.SOLD_OUT)
                .set(Screening::getUpdateTime, LocalDateTime.now())
                .eq(Screening::getId, screeningId)
                .eq(Screening::getDeleted, DeleteStatus.NORMAL)
                .eq(Screening::getAvailableTickets, ScreeningConstants.STOCK_EMPTY)
                .ne(Screening::getStatus, ScreeningStatusEnum.SOLD_OUT)
                .update();
    }


    @Override
    public ScreeningOrderSnapshot getOrderSnapshot(Long screeningId) {
        Screening s = lambdaQuery()
                .select(
                        Screening::getId,
                        Screening::getMerchantId,
                        Screening::getMovieId,
                        Screening::getBasePrice,
                        Screening::getStartTime
                )
                .eq(Screening::getId, screeningId)
                .eq(Screening::getDeleted, DeleteStatus.NORMAL)
                .one();

        if (s == null) return null;

        ScreeningOrderSnapshot snap = new ScreeningOrderSnapshot();
        snap.setId(s.getId());
        snap.setMerchantId(s.getMerchantId());
        snap.setMovieId(s.getMovieId());
        snap.setBasePrice(s.getBasePrice());
        snap.setStartTime(s.getStartTime());
        return snap;
    }

    /**
     * 订单超时回滚库存
     */
    @Override
    public boolean incrStockAndDecrSold(Long screeningId, Integer cnt) {
        return lambdaUpdate()
                .setSql("available_tickets = available_tickets + " + cnt)
                .setSql("sold_tickets = sold_tickets - " + cnt)
                .setSql("version = version + 1")
                .set(Screening::getUpdateTime, LocalDateTime.now())
                .eq(Screening::getId, screeningId)
                .eq(Screening::getDeleted, DeleteStatus.NORMAL)
                .ge(Screening::getSoldTickets, cnt)
                .update();
    }

    /**
     * 售罄 → 恢复售卖
     */
    @Override
    public void markSellingIfHasStock(Long screeningId) {
        lambdaUpdate()
                .set(Screening::getStatus, ScreeningStatusEnum.SELLING)
                .set(Screening::getUpdateTime, LocalDateTime.now())
                .eq(Screening::getId, screeningId)
                .eq(Screening::getDeleted, DeleteStatus.NORMAL)
                .eq(Screening::getStatus, ScreeningStatusEnum.SOLD_OUT)
                .gt(Screening::getAvailableTickets, ScreeningConstants.STOCK_EMPTY)
                .update();
    }
}
package com.meteor.ticketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.ticketing.domain.entity.HotRank;
import com.meteor.ticketing.mapper.HotRankMapper;
import com.meteor.ticketing.service.IHotRankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 场次热度榜 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
@RequiredArgsConstructor
public class HotRankServiceImpl extends ServiceImpl<HotRankMapper, HotRank> implements IHotRankService {

    @Override
    public Map<Long, Integer> getScoreMap(List<Long> screeningIds) {
        if (screeningIds == null || screeningIds.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = screeningIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        List<HotRank> rows = this.list(
                new LambdaQueryWrapper<HotRank>()
                        .eq(HotRank::getDeleted, DeleteStatus.NORMAL.getCode())
                        .in(HotRank::getScreeningId, ids)
                        .select(HotRank::getScreeningId, HotRank::getScore)
        );


        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> map = new HashMap<>((int) (rows.size() / 0.75f) + 1);
        for (HotRank r : rows) {
            if (r == null || r.getScreeningId() == null) {
                continue;
            }
            map.put(r.getScreeningId(), r.getScore() == null ? 0 : r.getScore());
        }
        return map;
    }
}

package com.meteor.ticketing.service;

import com.meteor.ticketing.domain.entity.HotRank;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 场次热度榜 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface IHotRankService extends IService<HotRank> {

    Map<Long, Integer> getScoreMap(List<Long> screeningIds);
}

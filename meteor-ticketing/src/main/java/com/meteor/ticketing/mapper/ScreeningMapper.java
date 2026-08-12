package com.meteor.ticketing.mapper;

import com.meteor.ticketing.domain.entity.Screening;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.lettuce.core.dynamic.annotation.Param;

import java.time.LocalDateTime;

/**
 * <p>
 * 电影场次表 Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface ScreeningMapper extends BaseMapper<Screening> {

    /**
     * 减少可售票数量，增加已售票数量
     *
     * @param screeningId 场次ID
     * @param quantity 票数量
     * @param updateTime 更新时间
     * @return 影响行数
     *
     * */
    int decreaseAvailableAndIncreaseSold(
            @Param("screeningId") Long screeningId,
            @Param("quantity") Integer quantity,
            @Param("updateTime") LocalDateTime updateTime
    );

    /**
     * 增加可售票数量，减少已售票数量
     *
     * @param screeningId 场次ID
     * @param quantity 票数量
     * @param updateTime 更新时间
     * @return 影响行数
     *
     * */
    int increaseAvailableAndDecreaseSold(
            @Param("screeningId") Long screeningId,
            @Param("quantity") Integer quantity,
            @Param("updateTime") LocalDateTime updateTime
    );
}



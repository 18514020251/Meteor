package com.meteor.ticketing.mapper;

import com.meteor.ticketing.domain.entity.Screening;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

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
     * 扣减库存并增加已售
     *
     * @return 影响行数：1=成功 0=失败（库存不足/不存在/已删除/不允许售卖）
     */
    @Update("""
        UPDATE screening
        SET available_tickets = available_tickets - 1,
            sold_tickets      = sold_tickets + 1,
            version           = version + 1,
            update_time       = NOW()
        WHERE id = #{screeningId}
          AND deleted = 0
          AND available_tickets > 0
        """)
    int decrStockAndIncrSold(Long screeningId);

    @Update("""
    UPDATE screening
    SET status = 3,
        update_time = NOW()
    WHERE id = #{screeningId}
      AND deleted = 0
      AND available_tickets = 0
      AND status <> 3
    """)
    int markSoldOutIfNeeded(Long screeningId);

}

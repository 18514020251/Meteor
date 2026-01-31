package com.meteor.message.mapper;

import com.meteor.message.domain.entity.UserMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户消息表(收件箱，记录已读/删除状态) Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-01-29
 */
public interface UserMessageMapper extends BaseMapper<UserMessage> {

    @Delete("""
        DELETE FROM user_message
        WHERE deleted = 1
          AND create_time < #{cutoff}
        ORDER BY id
        LIMIT #{limit}
    """)
    int physicalDeleteExpired(@Param("cutoff") LocalDateTime cutoff,
                              @Param("limit") int limit);
}

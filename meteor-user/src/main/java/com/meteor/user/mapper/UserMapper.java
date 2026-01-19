package com.meteor.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meteor.user.domain.entiey.User;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-01-13
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("""
        SELECT *
        FROM user
        WHERE phone = #{phone}
        AND is_deleted = 0
        LIMIT 1
    """)
    User selectByPhone(String phone);

    @Select("""
    SELECT *
    FROM user
    WHERE username = #{username}
    AND is_deleted = 0
    LIMIT 1
""")
    User selectByUsername(String username);
}

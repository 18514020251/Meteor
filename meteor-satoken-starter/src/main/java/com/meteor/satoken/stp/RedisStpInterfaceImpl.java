package com.meteor.satoken.stp;

import cn.dev33.satoken.stp.StpInterface;
import com.meteor.common.cache.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;

/**
 * @author Programmer
 */
@Slf4j
@RequiredArgsConstructor
public class RedisStpInterfaceImpl implements StpInterface {

    private final StringRedisTemplate redisTemplate;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            String key = RedisKeyConstants.buildUserRoleKey(loginId.toString());
            String rolesStr = redisTemplate.opsForValue().get(key);
            if (rolesStr == null || rolesStr.isEmpty()) {
                return Collections.emptyList();
            }
            return List.of(rolesStr.split(","));
        } catch (Exception e) {
            log.error("Failed to get role list for loginId: {}", loginId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}

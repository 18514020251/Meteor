package com.meteor.admin.service.impl;

import com.meteor.admin.controller.vo.OnlineUserVO;
import com.meteor.admin.service.IUserCacheService;
import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-08 21:57
 */
@Service
@RequiredArgsConstructor
public class AdminOnlineUserServiceImpl implements IUserCacheService {
    private final StringRedisTemplate redisTemplate;

    @Override
    public PageResult<OnlineUserVO> pageOnlineUsers(int pageNum, int pageSize) {

        String zsetKey = RedisKeyConstants.onlineUserZsetKey();

        long start = (pageNum - 1L) * pageSize;
        long end = start + pageSize - 1;

        Set<String> userIds = redisTemplate.opsForZSet()
                .reverseRange(zsetKey, start, end);

        Long total = redisTemplate.opsForZSet().zCard(zsetKey);
        if (total == null) total = 0L;

        if (userIds == null || userIds.isEmpty()) {
            return new PageResult<>(List.of(), total, pageNum, pageSize);
        }

        List<OnlineUserVO> list = new ArrayList<>();
        for (String uid : userIds) {
            String detailKey = RedisKeyConstants.buildOnlineUserDetailKey(uid);
            Map<Object, Object> map = redisTemplate.opsForHash().entries(detailKey);
            if (map.isEmpty()) continue;

            list.add(new OnlineUserVO(
                    uid,
                    str(map.get(RedisKeyConstants.ONLINE_USER_FIELD_IP)),
                    str(map.get(RedisKeyConstants.ONLINE_USER_FIELD_ROLE)),
                    longVal(map.get(RedisKeyConstants.ONLINE_USER_FIELD_LOGIN_TIME))
            ));
        }

        return new PageResult<>(list, total, pageNum, pageSize);
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private Long longVal(Object o) {
        if (o == null) return 0L;
        try { return Long.valueOf(String.valueOf(o)); } catch (Exception e) { return 0L; }
    }
}

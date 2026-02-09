package com.meteor.ticketing.job;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.meteor.ticketing.mapper.HotRankMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 *  刷新电影热度
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-08 12:00
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieHotFlushJob {

    private final StringRedisTemplate redisTemplate;
    private final HotRankMapper hotRankMapper;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60_000)
    public void flushMovieHot() {

        Set<String> keys = redisTemplate.keys("movie:hot:*");
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        for (String key : keys) {
            try {
                Long movieId = extractMovieId(key);
                String hotStr = redisTemplate.opsForValue().get(key);

                if (hotStr != null) {
                    long hot = Long.parseLong(hotStr);
                    if (hot > 0) {
                        hotRankMapper.increaseMovieHot(movieId, hot);
                        redisTemplate.delete(key);
                    }
                }

            } catch (Exception e) {
                log.warn("flush movie hot failed, key={}", key, e);
            }
        }
    }

    private Long extractMovieId(String key) {
        return Long.valueOf(key.substring(key.lastIndexOf(":") + 1));
    }
}

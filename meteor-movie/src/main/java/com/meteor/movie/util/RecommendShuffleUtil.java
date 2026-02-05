package com.meteor.movie.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *  推荐列表打乱工具类
 *
 * @author Programmer
 * @date 2026-02-04 14:15
 */
public class RecommendShuffleUtil {

    private RecommendShuffleUtil(){

    }

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 稳定打乱：
     * 同一个 uid 在同一天顺序一致
     */
    public static <T> void shuffle(List<T> list, Long uid) {

        if (list == null || list.size() <= 1 || uid == null) {
            return;
        }

        long daySeed = Long.parseLong(LocalDate.now().format(DF));

        long seed = uid * 31 + daySeed;

        Collections.shuffle(list, new Random(seed));
    }
}

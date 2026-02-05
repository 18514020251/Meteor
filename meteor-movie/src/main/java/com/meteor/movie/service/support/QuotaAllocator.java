package com.meteor.movie.service.support;

import com.meteor.movie.remote.user.dto.UserPreferenceCategorySummaryDTO;

import java.util.*;

/**
 *  配额分配器
 *
 * @author Programmer
 * @date 2026-02-04 14:13
 */
public class QuotaAllocator {

    private static final int TOTAL_SIZE = 8;
    private static final int TOP_K = 4;

    /**
     * 输入：用户偏好列表
     * 输出：categoryId -> quota
     */
    public static Map<Long, Integer> allocate(List<UserPreferenceCategorySummaryDTO> items) {

        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }

        // 排序 取Top4
        List<UserPreferenceCategorySummaryDTO> sorted = items.stream()
                .sorted(Comparator
                        .comparing(UserPreferenceCategorySummaryDTO::getScore).reversed()
                        .thenComparing(UserPreferenceCategorySummaryDTO::getLastTouchTime, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .limit(TOP_K)
                .toList();

        // 总分和
        int totalScore = sorted.stream()
                .mapToInt(UserPreferenceCategorySummaryDTO::getScore)
                .sum();

        // 兜底
        if (totalScore <= 0) {
            Map<Long, Integer> avg = new LinkedHashMap<>();
            int base = TOTAL_SIZE / sorted.size();
            int remain = TOTAL_SIZE % sorted.size();

            for (UserPreferenceCategorySummaryDTO item : sorted) {
                avg.put(item.getCategoryId(), base);
            }
            Iterator<Long> it = avg.keySet().iterator();
            while (remain-- > 0 && it.hasNext()) {
                Long key = it.next();
                avg.put(key, avg.get(key) + 1);
            }
            return avg;
        }

        Map<Long, Integer> result = new LinkedHashMap<>();
        List<Remainder> remainders = new ArrayList<>();

        int used = 0;

        for (UserPreferenceCategorySummaryDTO item : sorted) {

            double raw = (double) TOTAL_SIZE * item.getScore() / totalScore;

            int base = (int) Math.floor(raw);
            double rem = raw - base;

            result.put(item.getCategoryId(), base);
            remainders.add(new Remainder(item.getCategoryId(), rem));

            used += base;
        }

        int remain = TOTAL_SIZE - used;

        remainders.sort(Comparator.comparing(Remainder::remainder).reversed());

        for (int i = 0; i < remain && i < remainders.size(); i++) {
            Long cid = remainders.get(i).categoryId();
            result.put(cid, result.get(cid) + 1);
        }

        return result;
    }

    private record Remainder(Long categoryId, double remainder) {}
}

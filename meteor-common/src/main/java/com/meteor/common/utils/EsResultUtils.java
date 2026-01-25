package com.meteor.common.utils;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import java.util.ArrayList;
import java.util.List;

/**
 *  ES查询工具类
 *
 * @author Programmer
 * @date 2026-01-25 10:51
 */
public class EsResultUtils {

    private EsResultUtils() {}

    public static <T> List<T> parseHits(SearchResponse<T> response) {
        List<T> result = new ArrayList<>();
        for (Hit<T> hit : response.hits().hits()) {
            if (hit.source() != null) {
                result.add(hit.source());
            }
        }
        return result;
    }

}

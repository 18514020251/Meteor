package com.meteor.movie.service.impl;

import com.meteor.api.contract.ticketing.client.TicketingClient;
import com.meteor.api.contract.ticketing.dto.TicketingMovieInfoListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *   ticketing 查询服务
 *
 * @author Programmer
 * @date 2026-02-04 17:16
 */
@Service
@RequiredArgsConstructor
public class TicketingQueryService {

    private final TicketingClient ticketingClient;

    public Map<Long, TicketingMovieInfoListDTO.Item> getInfoMap(List<Long> movieIds) {

        if (movieIds == null || movieIds.isEmpty()) {
            return Map.of();
        }

        final TicketingMovieInfoListDTO dto;
        try {
            dto = ticketingClient.getMovieInfo(movieIds);
        } catch (Exception e) {
            return Map.of();
        }

        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            return Map.of();
        }

        Map<Long, TicketingMovieInfoListDTO.Item> map = new HashMap<>(dto.getItems().size() * 2);
        for (var item : dto.getItems()) {
            if (item != null && item.getMovieId() != null) {
                map.put(item.getMovieId(), item);
            }
        }
        return map;
    }
}

package com.meteor.api.contract.ticketing.client;

import com.meteor.api.contract.ticketing.dto.TicketingMovieInfoListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *  ticketing 服务远程契约
 *
 * @author Programmer
 * @date 2026-02-05 12:04
 */
@FeignClient(
        name = "meteor-ticketing",
        contextId = "ticketingClient",
        url = "${meteor.remote.ticketing-base-url}"
)
public interface TicketingClient {

    @GetMapping("/internal/ticketing/movie-info")
    TicketingMovieInfoListDTO getMovieInfo(@RequestParam("movieIds") List<Long> movieIds);
}

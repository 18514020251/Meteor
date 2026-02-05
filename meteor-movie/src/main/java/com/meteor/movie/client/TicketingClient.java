package com.meteor.movie.client;

import com.meteor.movie.controller.dto.TicketingMovieInfoListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *  远程调用接口 ticketing
 *
 * @author Programmer
 * @date 2026-02-04 16:53
 */
@FeignClient(
        name = "meteor-ticketing",
        url = "${meteor.remote.ticketing-base-url}"
)
public interface TicketingClient {

    @GetMapping("/internal/ticketing/movie-info")
    TicketingMovieInfoListDTO getMovieInfo(@RequestParam("movieIds") List<Long> movieIds);
}

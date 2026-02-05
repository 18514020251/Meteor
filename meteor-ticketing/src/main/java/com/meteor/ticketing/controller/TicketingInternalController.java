package com.meteor.ticketing.controller;

import com.meteor.api.contract.ticketing.dto.TicketingMovieInfoListDTO;
import com.meteor.ticketing.service.IScreeningService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  内部controller
 *
 * @author Programmer
 * @date 2026-02-04 16:58
 */
@RequestMapping("/internal/ticketing")
@RequiredArgsConstructor
@RestController
public class TicketingInternalController {

    private final IScreeningService service;

    @Operation(summary = "内部接口 获取电影信息")
    @GetMapping("/movie-info")
    public TicketingMovieInfoListDTO movieInfo(@RequestParam List<Long> movieIds) {
        return service.batchGetMovieInfo(movieIds);
    }

}

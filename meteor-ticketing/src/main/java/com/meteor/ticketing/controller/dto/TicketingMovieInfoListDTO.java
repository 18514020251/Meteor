package com.meteor.ticketing.controller.dto;

import lombok.Data;

import java.util.List;

/**
 *  票务-电影信息 DTO
 *
 * @author Programmer
 * @date 2026-02-04 16:52
 */
@Data
public class TicketingMovieInfoListDTO {

    private List<Item> items;

    @Data
    public static class Item {
        private Long movieId;
        private Integer price;
        private Boolean inGrabPeriod;
        private Integer hotScore;
    }
}


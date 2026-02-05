package com.meteor.api.contract.ticketing.dto;

import lombok.Data;

import java.util.List;

/**
 *  ticketing 服务：电影票务信息批量查询返回 DTO（内部接口）
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

package com.meteor.ticketing.service;

import com.meteor.ticketing.controller.vo.GrabOrderVO;
import jakarta.validation.constraints.NotNull;

/**
 *  抢票下单服务
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 11:43
 */
public interface IGrabOrderService {

    /**
     *  抢票下单
     *
     * @param screeningId 影片排期ID
     * @return 抢票结果
     */
    GrabOrderVO grab(@NotNull Long screeningId , Long uid);
}

package com.meteor.ticketing.service;

import com.meteor.api.contract.ticketing.dto.TicketingMovieInfoListDTO;
import com.meteor.ticketing.controller.dto.screening.ScreeningCreateDTO;
import com.meteor.ticketing.domain.entity.Screening;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 电影场次表 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface IScreeningService extends IService<Screening> {

    /**
     *  新增场次
     *
     * @param uid 用户id
     * @param dto 新增参数
     * */
    void create(Long uid, @Valid ScreeningCreateDTO dto);

    TicketingMovieInfoListDTO batchGetMovieInfo(List<Long> movieIds);
}

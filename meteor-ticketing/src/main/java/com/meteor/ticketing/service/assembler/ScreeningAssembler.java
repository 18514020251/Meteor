package com.meteor.ticketing.service.assembler;

import com.meteor.ticketing.controller.dto.screening.ScreeningCreateDTO;
import com.meteor.ticketing.controller.enums.ScreeningStatusEnum;
import com.meteor.ticketing.domain.entity.Screening;

import java.time.LocalDateTime;

/**
 *  场次实体组装器
 *
 * @author Programmer
 * @date 2026-02-02 11:41
 */
public final class ScreeningAssembler {

    private ScreeningAssembler() {}

    /**
     * 新建场次：把 DTO + 派生字段组装成实体
     */
    public static Screening toNewEntity(Long merchantId, ScreeningCreateDTO dto, ScreeningStatusEnum initStatus, LocalDateTime now) {
        return new Screening()
                .setMerchantId(merchantId)
                .setMovieId(dto.getMovieId())
                .setStartTime(dto.getStartTime())
                .setEndTime(dto.getEndTime())
                .setSaleStartTime(dto.getSaleStartTime())
                .setSaleEndTime(dto.getSaleEndTime())
                .setStatus(initStatus)
                .setSaleMode(dto.getSaleMode())
                .setBasePrice(dto.getBasePrice())
                .setTotalTickets(dto.getTotalTickets())
                .initForCreate(merchantId, now);
    }

}

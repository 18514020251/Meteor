package com.meteor.ticketing.service.impl;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.ticketing.controller.dto.screening.ScreeningCreateDTO;
import com.meteor.ticketing.controller.enums.ScreeningStatusEnum;
import com.meteor.ticketing.domain.entity.Screening;
import com.meteor.ticketing.mapper.ScreeningMapper;
import com.meteor.ticketing.service.IScreeningService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.ticketing.service.assembler.ScreeningAssembler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 电影场次表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
public class ScreeningServiceImpl extends ServiceImpl<ScreeningMapper, Screening> implements IScreeningService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long uid, ScreeningCreateDTO dto) {

        validateCreate(dto);

        LocalDateTime now = LocalDateTime.now();
        ScreeningStatusEnum initStatus = calcInitStatus(dto.getSaleStartTime(), dto.getSaleEndTime(), now);

        Screening screening = ScreeningAssembler.toNewEntity(uid, dto, initStatus, now);

        int rows = baseMapper.insert(screening);
        if (rows != 1) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "新增场次失败");
        }
    }


    private void validateCreate(ScreeningCreateDTO dto) {

        LocalDateTime start = dto.getStartTime();
        LocalDateTime end = dto.getEndTime();
        LocalDateTime saleStart = dto.getSaleStartTime();
        LocalDateTime saleEnd = dto.getSaleEndTime();

        if (saleStart.isAfter(start)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "开售时间不能晚于开场时间");
        }

        if (end != null && !end.isAfter(start)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "散场时间必须晚于开场时间");
        }

        if (saleEnd != null && saleEnd.isBefore(saleStart)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "停售时间不能早于开售时间");
        }

        if (saleEnd != null && saleEnd.isAfter(start)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "停售时间不能晚于开场时间");
        }
    }

    private ScreeningStatusEnum calcInitStatus(
            LocalDateTime saleStart,
            LocalDateTime saleEnd,
            LocalDateTime now) {

        if (now.isBefore(saleStart)) {
            return ScreeningStatusEnum.SCHEDULED;
        }

        if (saleEnd != null && now.isAfter(saleEnd)) {
            return ScreeningStatusEnum.CLOSED;
        }

        return ScreeningStatusEnum.SELLING;
    }
}
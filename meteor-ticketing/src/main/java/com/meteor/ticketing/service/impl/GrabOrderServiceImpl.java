package com.meteor.ticketing.service.impl;

import com.meteor.api.enums.GrabOrderResultEnum;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.mq.publisher.TicketOrderEventPublisher;
import com.meteor.ticketing.service.IGrabOrderService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.service.cache.model.RedisStockOpResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 抢票下单服务实现类
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 11:44
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class GrabOrderServiceImpl implements IGrabOrderService {

    private final TicketOrderEventPublisher publisher;
    private final SnowflakeIdGenerator idGenerator;
    private final ITicketingStockRedisService stockRedisService;


    @Override
    public GrabOrderVO grab(Long screeningId, Long userId) {

        if (!stockRedisService.isSaleStarted(screeningId)) {
            return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY);
        }

        RedisStockOpResult r = stockRedisService.decrStock1(screeningId);

        switch (r.code()) {
            case SUCCESS -> { /* 继续 */ }
            case SOLD_OUT -> { return GrabOrderVO.of(GrabOrderResultEnum.SOLD_OUT); }
            case NOT_READY -> { return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY); }
            default -> { return GrabOrderVO.of(GrabOrderResultEnum.FAIL); }
        }

        String orderNo = String.valueOf(idGenerator.nextId());
        publisher.publishCreateOrThrow(orderNo, userId, screeningId);

        return GrabOrderVO.of(GrabOrderResultEnum.SUCCESS, orderNo, r.left());
    }

}

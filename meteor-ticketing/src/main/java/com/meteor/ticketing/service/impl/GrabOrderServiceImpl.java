package com.meteor.ticketing.service.impl;

import com.meteor.api.enums.GrabOrderResultEnum;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.mq.publisher.TicketOrderEventPublisher;
import com.meteor.ticketing.redis.RedisScripts;
import com.meteor.ticketing.service.IGrabOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.meteor.common.cache.RedisKeyConstants.buildScreeningStockKey;

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
    private final StringRedisTemplate stringRedisTemplate;
    private final SnowflakeIdGenerator idGenerator;


    /**
     * 抢票核心流程：
     * Lua扣库存 -> 发MQ
     */
    public GrabOrderVO grab(Long screeningId, Long userId) {

        String stockKey = buildScreeningStockKey(screeningId);

        Long left = stringRedisTemplate.execute(
                RedisScripts.DECR_STOCK_1,
                List.of(stockKey)
        );

        if (left == null) {
            return GrabOrderVO.of(GrabOrderResultEnum.FAIL);
        }
        if (left == -3) {
            return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY);
        }
        if (left == -1) {
            return GrabOrderVO.of(GrabOrderResultEnum.SOLD_OUT);
        }

        String orderNo = String.valueOf(idGenerator.nextId());

        publisher.publishCreateOrThrow(orderNo, userId, screeningId);

        return GrabOrderVO.of(GrabOrderResultEnum.SUCCESS, orderNo, left);
    }
}

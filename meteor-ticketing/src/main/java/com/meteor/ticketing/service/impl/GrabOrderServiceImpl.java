package com.meteor.ticketing.service.impl;

import com.meteor.api.enums.GrabOrderResultEnum;
import com.meteor.mq.contract.ticketing.TicketOrderContract;
import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.redis.RedisScripts;
import com.meteor.ticketing.service.IGrabOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 抢票核心流程：
     * Lua扣库存 -> 发MQ
     */
    public GrabOrderVO grab(Long screeningId, Long userId) {
        log.info("[Grab] uid={} screeningId={} stockKey={}", userId, screeningId, buildScreeningStockKey(screeningId));


        String stockKey = buildScreeningStockKey(screeningId);

        Long result = stringRedisTemplate.execute(
                RedisScripts.DECR_STOCK_1,
                List.of(stockKey)
        );

        if (result == null) {
            return GrabOrderVO.of(GrabOrderResultEnum.FAIL);
        }

        if (result == -3) {
            return GrabOrderVO.of(GrabOrderResultEnum.NOT_READY);
        }
        if (result == -1) {
            return GrabOrderVO.of(GrabOrderResultEnum.SOLD_OUT);
        }

        String orderNo = UUID.randomUUID().toString().replace("-", "");


        TicketOrderCreateMessage msg = new TicketOrderCreateMessage(orderNo , userId , screeningId , LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                TicketOrderContract.Exchange.TICKET_ORDER,
                TicketOrderContract.RoutingKey.TICKET_ORDER_CREATE,
                msg
        );

        return GrabOrderVO.of(
                GrabOrderResultEnum.SUCCESS,
                orderNo,
                result
        );
    }
}

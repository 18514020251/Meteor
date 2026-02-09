package com.meteor.ticketing.mq.assmabler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import com.meteor.mq.contract.ticketing.TicketOrderDbReservedMessage;
import com.meteor.ticketing.controller.dto.ScreeningOrderSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 *  订单数据库预留消息转换器
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 17:50
 */
@Component
@RequiredArgsConstructor
public class TicketOrderDbReservedMessageAssembler {

    private final ObjectMapper objectMapper;

    public TicketOrderDbReservedMessage from(
            TicketOrderCreateMessage createMsg,
            ScreeningOrderSnapshot screening
    ) {

        TicketOrderDbReservedMessage msg = new TicketOrderDbReservedMessage();
        msg.setOrderNo(createMsg.getOrderNo());
        msg.setUserId(createMsg.getUserId());
        msg.setMerchantId(screening.getMerchantId());
        msg.setScreeningId(screening.getId());
        msg.setMovieId(screening.getMovieId());
        msg.setUnitPrice(screening.getBasePrice());
        msg.setCreateTime(LocalDateTime.now());

        msg.setSnapshotJson(buildSnapshot(screening));

        return msg;
    }

    private String buildSnapshot(ScreeningOrderSnapshot s) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("screeningId", s.getId());
        snapshot.put("startTime", s.getStartTime());
        snapshot.put("price", s.getBasePrice());

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "snapshot serialize failed,ExMessage:" + e);
        }
    }
}

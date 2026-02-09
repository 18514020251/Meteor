package com.meteor.order.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 *  订单 MQ 消费日志 Mapper 接口
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 17:14
 */
@Mapper
public interface OrderMqConsumeLogMapper {

    @Insert("""
        INSERT INTO order_mq_consume_log (msg_key, topic, create_time)
        VALUES (#{orderNo}, #{topic}, #{now})
    """)
    void insert(@Param("orderNo") String orderNo,
                @Param("topic") String topic,
                @Param("now") LocalDateTime now);
}


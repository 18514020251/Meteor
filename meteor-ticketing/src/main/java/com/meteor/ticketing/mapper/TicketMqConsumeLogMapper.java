package com.meteor.ticketing.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 *  MQ消费日志 Mapper 接口
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 16:49
 */
@Mapper
public interface TicketMqConsumeLogMapper {

    @Insert("""
        INSERT INTO ticket_mq_consume_log(msg_key, topic, create_time)
        VALUES(#{msgKey}, #{topic}, #{createTime})
        """)
    int insert(String msgKey, String topic, LocalDateTime createTime);
}

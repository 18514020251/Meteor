package com.meteor.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meteor.analytics.domain.entity.OpMqFailMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  MQ 消息补发 Mapper
 * */
@Mapper
public interface OpMqFailMsgMapper extends BaseMapper<OpMqFailMsg> {

    List<OpMqFailMsg> selectPendingForResend(@Param("doingTimeoutMinutes") int doingTimeoutMinutes,
                                             @Param("limit") int limit);
}

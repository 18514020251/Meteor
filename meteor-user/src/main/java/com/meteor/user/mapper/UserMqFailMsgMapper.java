package com.meteor.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meteor.api.model.UserMqFailureEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 *  MQ失败消息
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 14:56
 */
@Mapper
public interface UserMqFailMsgMapper extends BaseMapper<UserMqFailureEntity> {
}

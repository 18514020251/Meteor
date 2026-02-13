package com.meteor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.api.model.UserMqFailureEntity;
import com.meteor.common.enums.system.mq.MessageStatusEnum;
import com.meteor.user.mapper.UserMqFailMsgMapper;
import com.meteor.user.service.IUserMqFailMsgService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  MQ失败消息服务实现类
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 14:57
 */
@Service
public class UserMqFailMsgServiceImpl
        extends ServiceImpl<UserMqFailMsgMapper, UserMqFailureEntity>
        implements IUserMqFailMsgService {
    @Override
    public List<UserMqFailureEntity> getRecentFailedMessages(Integer pastMinutes) {
        LocalDateTime startTime = LocalDateTime.now()
                .minusMinutes(pastMinutes)
                .minusSeconds(((pastMinutes - (long) pastMinutes) * 60));

        LambdaQueryWrapper<UserMqFailureEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserMqFailureEntity::getStatus, MessageStatusEnum.FAILED, MessageStatusEnum.PENDING)
                .ge(UserMqFailureEntity::getCreateTime, startTime);

        return baseMapper.selectList(queryWrapper);
    }
}

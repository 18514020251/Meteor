package com.meteor.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.admin.mapper.MqFailMsgMapper;
import com.meteor.admin.service.IMqFailMsgService;
import com.meteor.api.model.AdminMqFailureEntity;
import com.meteor.common.enums.system.mq.MessageStatusEnum;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  MQ失败消息服务实现类
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 11:05
 */
@Service
public class MqFailMsgServiceImpl extends ServiceImpl<MqFailMsgMapper, AdminMqFailureEntity> implements IMqFailMsgService{

    @Override
    public List<AdminMqFailureEntity> getRecentFailedMessages(Integer pastMinutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(pastMinutes)
                .minusSeconds(((pastMinutes - (long) pastMinutes) * 60));

        LambdaQueryWrapper<AdminMqFailureEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AdminMqFailureEntity::getStatus, MessageStatusEnum.FAILED, MessageStatusEnum.PENDING)
                .ge(AdminMqFailureEntity::getCreateTime, startTime);

        return baseMapper.selectList(queryWrapper);
    }
}

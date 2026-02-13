package com.meteor.api.service.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.api.model.AbstractMqFailureEntity;
import com.meteor.common.enums.system.mq.MessageStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通用 MQ 失败消息服务基类
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 10:46
 *
 * @param <M> Mapper 类型
 * @param <T> Entity 类型
 */
public abstract class BaseMqFailureService<M extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T>, T extends AbstractMqFailureEntity>
        extends ServiceImpl<M, T> {

    /**
     * 查询最近几分钟内的 MQ 失败和待处理消息
     *
     * @param pastMinutes 时间范围，单位分钟
     * @return 查询结果列表
     */
    public List<T> getRecentFailedMessages(double pastMinutes) {
        LocalDateTime startTime = LocalDateTime.now()
                .minusMinutes((long) pastMinutes)
                .minusSeconds((long) ((pastMinutes - (long) pastMinutes) * 60));

        LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(T::getStatus, MessageStatusEnum.FAILED, MessageStatusEnum.PENDING)
                .ge(T::getCreateTime, startTime);

        return baseMapper.selectList(queryWrapper);
    }
}

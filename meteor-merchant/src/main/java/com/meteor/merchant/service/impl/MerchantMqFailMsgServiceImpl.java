package com.meteor.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.api.model.MerchantMqFailureEntity;
import com.meteor.common.enums.system.mq.MessageStatusEnum;
import com.meteor.merchant.mapper.MerchantMqFailMsgMapper;
import com.meteor.merchant.service.IMerchantMqFailMsgService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  商家MQ失败消息
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 18:14
 */
@Service
public class MerchantMqFailMsgServiceImpl
        extends ServiceImpl<MerchantMqFailMsgMapper, MerchantMqFailureEntity>
        implements IMerchantMqFailMsgService {

    @Override
    public List<MerchantMqFailureEntity> getRecentFailedMessages(Integer pastMinutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(pastMinutes).minusSeconds(((pastMinutes - (long) pastMinutes) * 60));

        LambdaQueryWrapper<MerchantMqFailureEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantMqFailureEntity::getStatus, MessageStatusEnum.FAILED , MessageStatusEnum.PENDING) // 失败状态
                .ge(MerchantMqFailureEntity::getCreateTime, startTime);


        return baseMapper.selectList(queryWrapper);
    }
}

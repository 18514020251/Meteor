package com.meteor.merchant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.api.model.MerchantMqFailureEntity;

import java.util.List;

/**
 *  商家MQ失败消息服务
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 18:13
 */
public interface IMerchantMqFailMsgService extends IService<MerchantMqFailureEntity> {

    /**
     * 查询近一段时间内的失败消息
     *
     * @param pastMinutes 时间范围，单位分钟
     * @return 近指定时间范围内的失败消息列表
     */
    List<MerchantMqFailureEntity> getRecentFailedMessages(Integer pastMinutes);
}

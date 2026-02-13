package com.meteor.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.api.model.UserMqFailureEntity;

import java.util.List;

/**
 *  MQ失败消息服务类
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 14:56
 */
public interface IUserMqFailMsgService extends IService<UserMqFailureEntity> {
    /**
     * 查询最近指定分钟内的失败记录
     *
     * @param pastMinutes 时间范围（分钟）
     * @return 用户 MQ 的失败消息列表
     */
    List<UserMqFailureEntity> getRecentFailedMessages(Integer pastMinutes);
}

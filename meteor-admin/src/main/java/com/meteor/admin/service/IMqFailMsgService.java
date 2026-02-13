package com.meteor.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.api.model.AdminMqFailureEntity;

import java.util.List;

/**
 *  MQ发送失败记录服务
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 10:52
 */
public interface IMqFailMsgService extends IService<AdminMqFailureEntity> {
    /**
     * 查询近一段时间内的失败和待处理的消息
     *
     * @param pastMinutes 时间范围，单位分钟
     * @return 失败消息的列表
     */
    List<AdminMqFailureEntity> getRecentFailedMessages(Integer pastMinutes);

}

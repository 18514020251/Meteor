package com.meteor.user.mq.support;

import com.meteor.common.enums.system.ModuleEnum;
import lombok.Builder;
import lombok.Getter;

/**
 *  MQ失败消息构建命令
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 15:18
 */
@Getter
@Builder
public class UserMqFailBuildCmd {

    private String msgId;
    private ModuleEnum module;
    private Long bizId;
    private String exchange;
    private String routingKey;
    private String topic;
    private Object payload;
    private String errorMsg;
}

package com.meteor.api.common.dtp;

import com.meteor.common.enums.system.ModuleEnum;
import com.meteor.common.enums.system.mq.MessageStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *  远程接口调用返回
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 17:54
 */
@Data
public class RemoteMqFailureDTO {
    private Long id;
    private String msgId;
    private ModuleEnum moduleName;
    private Long bizId;
    private String exchangeName;
    private String routingKey;
    private String topic;
    private String payload;

    private MessageStatusEnum status;
    private Integer retryCnt;
    private LocalDateTime nextRetryTime;
    private String lastError;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

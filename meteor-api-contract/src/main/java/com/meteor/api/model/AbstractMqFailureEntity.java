package com.meteor.api.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.meteor.common.enums.system.ModuleEnum;
import com.meteor.common.enums.system.mq.MessageStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ失败记录的通用实体类
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 9:18
 */
@Data
@TableName()
public abstract class AbstractMqFailureEntity {

    private Long id;
    // 消息唯一ID
    private String msgId;
    // 模块名称，例如 MERCHANT / USER
    private ModuleEnum moduleName;
    // 业务ID，例如商家ID/用户ID
    private Long bizId;
    // 交换机
    private String exchangeName;
    // 路由Key
    private String routingKey;
    // Topic
    private String topic;
    // 消息体（JSON）
    private String payload;

    // 0=PENDING, 1=DONE, 2=FAILED
    private MessageStatusEnum status;
    // 已重试次数
    private Integer retryCnt;
    // 下一次重试时间
    private LocalDateTime nextRetryTime;
    // 上次发送的错误
    private String lastError;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
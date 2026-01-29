package com.meteor.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户消息表
 * </p>
 *
 * @author Programmer
 * @since 2026-01-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_message")
@Schema(description="UserMessage对象")
public class UserMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID(消息归属)")
    private Long userId;

    @Schema(description = "消息来源：0系统消息 1业务事件(如MQ消费生成)")
    private Integer source;

    @Schema(description = "系统消息ID(source=0时关联sys_message.id)")
    private Long sysMsgId;

    @Schema(description = "消息类型(冗余字段，避免查询总是JOIN)")
    private Integer type;

    @Schema(description = "消息标题(冗余字段)")
    private String title;

    @Schema(description = "消息内容(冗余字段，短内容；长内容可改TEXT)")
    private String content;

    @Schema(description = "业务幂等键(可选)，用于防重复写入，如: merchantApply:123")
    private String bizKey;

    @Schema(description = "已读状态：0未读 1已读")
    private Integer readStatus;

    @Schema(description = "已读时间")
    private LocalDateTime readTime;

    @Schema(description = "删除标记：0正常 1已删除")
    @TableLogic
    private Integer deleted;

    @Schema(description = "创建时间(投递/生成时间)")
    private LocalDateTime createTime;


}

package com.meteor.message.domain.entity;

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
 * 系统消息表
 * </p>
 *
 * @author Programmer
 * @since 2026-01-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_message")
@Schema(description="系统消息表")
public class SysMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "业务幂等键(可选)，用于防重复创建，如: notice:20260129:001")
    private String bizKey;

    @Schema(description = "消息类型：1系统公告 2活动通知 3审核结果 4订单通知...(自定义枚举)")
    private Integer type;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容(长文本)")
    private String content;

    @Schema(description = "投递目标：0全体用户 1指定用户 2指定角色(预留)")
    private Integer target;

    @Schema(description = "创建人(管理员ID)")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "过期时间(为空表示不过期)")
    private LocalDateTime expireTime;

    @Schema(description = "删除标记：0正常 1已删除(软删)")
    private Integer deleted;


}

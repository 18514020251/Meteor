package com.meteor.user.domain.entiey;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.meteor.user.enums.RoleEnum;
import com.meteor.user.enums.UserStatus;
import lombok.*;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Programmer
 * @since 2026-01-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@Schema(description="用户表")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description  = "主键 ID" , example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名" , example = "admin")
    private String username;

    @Schema(description = "密码（加密存储）" , example = "123456")
    private String password;

    @Schema(description = "手机号" , example = "13800000000")
    private String phone;

    @Schema(description = "头像 URL" , example = "https:XX.com")
    private String avatar;

    @Schema(description = "用户状态：0-正常，1-禁用" , example = "0")
    private Integer status;

    @Schema(description = "角色：0-普通用户，1-商家，2-管理" , example = "0")
    private Integer role;

    @Schema(description = "最后登录时间" , example = "2026-01-13 15:56:00")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间" , example = "2026-01-13 15:56")
    private LocalDateTime createTime;

    @Schema(description = "更新时间" , example = "2026-01-13 15:56")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除" , example = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;

    public boolean isNormal(){
        return UserStatus.NORMAL.getCode().equals(this.status);
    }

    public boolean isUser(){
        return RoleEnum.USER.getCode().equals(this.role);
    }
}

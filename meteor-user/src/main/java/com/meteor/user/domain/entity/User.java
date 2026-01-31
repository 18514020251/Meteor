package com.meteor.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.meteor.common.constants.AvatarConstants;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.common.utils.PasswordUtil;
import com.meteor.user.enums.RoleEnum;
import com.meteor.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表
 *
 * @author Programmer
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@Schema(description = "用户表")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String phone;

    private String avatar;

    private Integer status;

    private Integer role;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;


    /**
     * 创建一个新注册用户
     */
    public static User createRegisterUser(String username, String rawPassword) {
        return User.builder()
                .username(username)
                .password(PasswordUtil.encrypt(rawPassword))
                .avatar(AvatarConstants.DEFAULT_AVATAR)
                .status(UserStatus.NORMAL.getCode())
                .role(RoleEnum.USER.getCode())
                .isDeleted(DeleteStatus.NORMAL.getCode())
                .build();
    }

    public boolean isNormal() {
        return UserStatus.NORMAL.getCode().equals(this.status);
    }

    public boolean isUser() {
        return RoleEnum.USER.getCode().equals(this.role);
    }

    public boolean deleted() {
        return DeleteStatus.DELETED.getCode().equals(this.isDeleted);
    }
}

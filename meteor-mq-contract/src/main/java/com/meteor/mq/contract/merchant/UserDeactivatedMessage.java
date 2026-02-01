package com.meteor.mq.contract.merchant;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户注销事件消息体
 * @author Programmer
 */
@Data
@AllArgsConstructor
public class UserDeactivatedMessage {

    private Long userId;
    private LocalDateTime timestamp;
}

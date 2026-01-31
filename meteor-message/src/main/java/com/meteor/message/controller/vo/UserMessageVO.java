package com.meteor.message.controller.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 *  用户消息VO
 *
 * @author Programmer
 * @date 2026-01-29 17:55
 */
@Data
public class UserMessageVO {
    private Long id;
    private Integer type;
    private String title;
    private String content;
    private Integer readStatus;
    private LocalDateTime createTime;
    private LocalDateTime readTime;
}

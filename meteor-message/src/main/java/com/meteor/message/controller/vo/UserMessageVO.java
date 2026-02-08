package com.meteor.message.controller.vo;


import java.time.LocalDateTime;

/**
 *  用户消息VO
 *
 * @author Programmer
 * @date 2026-01-29 17:55
 */
public record UserMessageVO(
    Long id,
    Integer type,
    String title,
    String content,
    Integer readStatus,
    LocalDateTime createTime,
    LocalDateTime readTime
){}

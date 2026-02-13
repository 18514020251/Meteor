package com.meteor.common.enums.system.mq;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 消息状态枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 11:41
 */
@Getter
public enum MessageStatusEnum {

    @Schema(description = "待处理")
    PENDING(0),

    @Schema(description = "已完成")
    DONE(1),

    @Schema(description = "已失败")
    FAILED(2);

    @EnumValue
    private final int code;

    MessageStatusEnum(int code) {
        this.code = code;
    }

    // 根据状态码获取对应的枚举
    public static MessageStatusEnum fromCode(int code) {
        for (MessageStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}

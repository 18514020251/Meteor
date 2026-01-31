package com.meteor.user.controller.dto;

import com.meteor.common.enums.user.VerifyCodeSceneEnum;
import lombok.Data;

/**
 *  手机验证码发送 DTO
 *
 * @author Programmer
 * @date 2026-01-20 22:45
 */
@Data
public class PhoneVerifyCodeSendDTO {

    private String phone;

    private VerifyCodeSceneEnum scene;
}

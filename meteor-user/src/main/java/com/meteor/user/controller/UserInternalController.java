package com.meteor.user.controller;

import com.meteor.api.constants.MqFailMsgConstants;
import com.meteor.api.contract.user.dto.UserProfileDTO;
import com.meteor.api.model.UserMqFailureEntity;
import com.meteor.user.service.IUserCategoryPreferenceService;
import com.meteor.user.service.IUserMqFailMsgService;
import com.meteor.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  用户内部接口
 *
 * @author Programmer
 * @date 2026-02-01 11:42
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/user")
@Tag(name = "用户模块内部接口", description = "用户模块内部接口")
public class UserInternalController {

    private final IUserService userService;
    private final IUserCategoryPreferenceService userPreferenceService;
    private final IUserMqFailMsgService userMqFailureService;

    @Operation(summary = "内部-根据用户ID获取基础信息（仅查询商家）")
    @GetMapping("/profile")
    public UserProfileDTO getProfile(@RequestParam Long userId) {
        return userService.getUserProfile(userId);
    }

    @Operation(summary = "内部-获取用户喜好分类列表")
    @GetMapping("/preference")
    public com.meteor.api.contract.user.dto.UserPreferenceCategoryListDTO listPreferenceCategories(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer source
    ) {
        return userPreferenceService.listPreferenceCategories(userId, limit, source);
    }

    /**
     * 查询最近 6.5 分钟内失败（FAILED）/待处理（PENDING）的消息
     *
     * @return 用户模块失败消息列表
     */
    @Operation(summary = "内部-查询最近 6.5 分钟内失败（FAILED）/待处理（PENDING）的消息")
    @GetMapping("/mqFailures/recentFailed")
    public List<UserMqFailureEntity> getRecentFailedMessages() {
        return userMqFailureService.getRecentFailedMessages(MqFailMsgConstants.DEFAULT_PAST_MINUTES);
    }

}

package com.meteor.user.service;

import com.meteor.user.domain.entity.UserCategoryPreference;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户分类偏好(多来源) 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-03
 */
public interface IUserCategoryPreferenceService extends IService<UserCategoryPreference> {

    void deleteByUserIdAndSource(Long userId, int code);
}

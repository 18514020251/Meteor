package com.meteor.user.service;

import com.meteor.user.controller.dto.UserPreferenceCategoryListDTO;
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

    /**
     *  根据用户ID和来源删除
     *  @param userId 用户ID
     *  @param code 来源
     * */
    void deleteByUserIdAndSource(Long userId, int code);

    /**
     *  根据用户ID和来源获取用户偏好分类列表
     *  @param userId 用户ID
     *  @param limit 数量限制
     *  @param source 来源
     * */
    UserPreferenceCategoryListDTO listPreferenceCategories(Long userId, Integer limit, Integer source);
}

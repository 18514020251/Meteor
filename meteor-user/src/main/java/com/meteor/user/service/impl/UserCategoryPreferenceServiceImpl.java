package com.meteor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.meteor.user.domain.entity.UserCategoryPreference;
import com.meteor.user.mapper.UserCategoryPreferenceMapper;
import com.meteor.user.service.IUserCategoryPreferenceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户分类偏好(多来源) 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-03
 */
@Service
public class UserCategoryPreferenceServiceImpl extends ServiceImpl<UserCategoryPreferenceMapper, UserCategoryPreference> implements IUserCategoryPreferenceService {

    @Override
    public void deleteByUserIdAndSource(Long userId, int sourceCode) {

        LambdaQueryWrapper<UserCategoryPreference> qw = Wrappers.<UserCategoryPreference>lambdaQuery()
                .eq(UserCategoryPreference::getUserId, userId)
                .eq(UserCategoryPreference::getSource, sourceCode);

        baseMapper.delete(qw);
    }
}

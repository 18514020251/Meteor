package com.meteor.user.service.assembler;

import com.meteor.user.domain.entity.UserCategoryPreference;
import com.meteor.common.enums.user.UserPreferenceSourceEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *  用户分类偏好(多来源) 转换器
 *
 * @author Programmer
 * @date 2026-02-03 21:14
 */
@Component
public class UserCategoryPreferenceAssembler {

    public List<UserCategoryPreference> toManualEntities(Long userId, List<Long> categoryIds) {

        LocalDateTime now = LocalDateTime.now();

        List<UserCategoryPreference> list = new ArrayList<>(categoryIds.size());

        for (Long cid : categoryIds) {
            UserCategoryPreference e = new UserCategoryPreference();
            e.setUserId(userId);
            e.setCategoryId(cid);
            e.setSource(UserPreferenceSourceEnum.MANUAL.getCode());
            e.setScore(100);
            e.setLastSeenTime(now);
            e.setCreateTime(now);
            e.setUpdateTime(now);
            list.add(e);
        }

        return list;
    }
}

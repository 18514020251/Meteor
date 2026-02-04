package com.meteor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.common.enums.user.UserPreferenceSourceEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.user.controller.dto.UserPreferenceCategoryListDTO;
import com.meteor.user.controller.dto.UserPreferenceCategorySummaryDTO;
import com.meteor.user.domain.entity.User;
import com.meteor.user.domain.entity.UserCategoryPreference;
import com.meteor.user.enums.UserPreferenceInitEnum;
import com.meteor.user.mapper.UserCategoryPreferenceMapper;
import com.meteor.user.mapper.UserMapper;
import com.meteor.user.service.IUserCategoryPreferenceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.user.service.assembler.UserPreferenceAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 用户分类偏好(多来源) 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-03
 */
@Service
@RequiredArgsConstructor
public class UserCategoryPreferenceServiceImpl extends ServiceImpl<UserCategoryPreferenceMapper, UserCategoryPreference> implements IUserCategoryPreferenceService {

    private final UserMapper userMapper;

    @Override
    public void deleteByUserIdAndSource(Long userId, int sourceCode) {

        LambdaQueryWrapper<UserCategoryPreference> qw = Wrappers.<UserCategoryPreference>lambdaQuery()
                .eq(UserCategoryPreference::getUserId, userId)
                .eq(UserCategoryPreference::getSource, sourceCode);

        baseMapper.delete(qw);
    }

    @Override
    public UserPreferenceCategoryListDTO listPreferenceCategories(Long userId, Integer limit, Integer source) {

        // 参数校验
        validateParams(userId , limit , source);

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() != null && Objects.equals(user.getIsDeleted(), DeleteStatus.DELETED.getCode())) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }
        boolean preferenceInited = user.getPreferenceInited() == UserPreferenceInitEnum.INITED;

        // 查询用户偏好分类列表摘要
        List<Map<String, Object>> rows = queryPreferenceSummaryRows(userId, limit, source);

        // 转换为DTO
        List<UserPreferenceCategorySummaryDTO> items = convertRows(rows);

        // 构建返回结果
        return UserPreferenceAssembler.toPreferenceCategoryListDTO(preferenceInited, items);
    }

    /**
     *  参数校验
     *
     *  @param userId 用户ID
     *  @param limit  数量限制
     *  @param source 来源
     * */
    private void validateParams(Long userId, Integer limit, Integer source) {
        if (userId == null || userId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }
        if (limit == null || limit <= 0 || limit > 100) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }
        if (source != null
                && !Objects.equals(source, UserPreferenceSourceEnum.MANUAL.getCode())
                && !Objects.equals(source, UserPreferenceSourceEnum.PURCHASE.getCode())
                && !Objects.equals(source, UserPreferenceSourceEnum.BROWSE.getCode())) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }
    }

    /**
     *  查询用户偏好分类列表摘要
     *
     *  @param userId 用户ID
     *  @param limit  数量限制
     *  @param source 来源
     *  @return 用户偏好分类列表摘要
     * */
    private List<Map<String, Object>> queryPreferenceSummaryRows(Long userId, Integer limit, Integer source) {

        QueryWrapper<UserCategoryPreference> qw = new QueryWrapper<>();

        qw.select(
                "category_id AS categoryId",
                "SUM(score) AS score",
                "MAX(IFNULL(last_seen_time, update_time)) AS lastTouchTime"
        ).eq("user_id", userId);

        if (source != null) {
            qw.eq("source", source);
        }

        qw.groupBy("category_id")
                .orderByDesc("score")
                .orderByDesc("lastTouchTime")
                .orderByDesc("categoryId")
                .last("LIMIT " + limit);

        return baseMapper.selectMaps(qw);
    }

    /**
     *  将数据库查询结果转换为DTO
     *
     *  @param rows 数据库查询结果
     *  @return DTO列表
     * */
    private List<UserPreferenceCategorySummaryDTO> convertRows(List<Map<String, Object>> rows) {

        List<UserPreferenceCategorySummaryDTO> items = new ArrayList<>(rows.size());

        for (Map<String, Object> row : rows) {

            Number categoryIdNum = (Number) row.get("categoryId");
            Number scoreNum = (Number) row.get("score");
            Object lastTouchTimeObj = row.get("lastTouchTime");

            if (categoryIdNum == null || scoreNum == null) {
                continue;
            }

            UserPreferenceCategorySummaryDTO dto = new UserPreferenceCategorySummaryDTO();
            dto.setCategoryId(categoryIdNum.longValue());
            dto.setScore(scoreNum.intValue());
            dto.setLastTouchTime(toLocalDateTime(lastTouchTimeObj));

            items.add(dto);
        }

        return items;
    }

    /**
     *  转换为LocalDateTime
     *
     *  @param value 值
     *  @return LocalDateTime
     * */
    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        if (value instanceof java.util.Date dt) {
            return dt.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return null;
    }
}

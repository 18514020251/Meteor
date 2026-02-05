package com.meteor.user.service.assembler;

import com.meteor.api.contract.user.dto.UserPreferenceCategorySummaryDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户偏好组装器
 *
 * @author Programmer
 * @date 2026-02-04 11:08
 */
@Component
public class UserPreferenceAssembler {

    private UserPreferenceAssembler() {}

    /**
     * 组装用户偏好分类列表DTO
     */
    public static com.meteor.api.contract.user.dto.UserPreferenceCategoryListDTO toPreferenceCategoryListDTO(
            boolean preferenceInited,
            List<UserPreferenceCategorySummaryDTO> items
    ) {
        com.meteor.api.contract.user.dto.UserPreferenceCategoryListDTO result = new com.meteor.api.contract.user.dto.UserPreferenceCategoryListDTO();
        result.setPreferenceInited(preferenceInited);
        result.setItems(items);
        return result;
    }
}

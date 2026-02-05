package com.meteor.api.contract.user.client;

import com.meteor.api.contract.user.dto.UserPreferenceCategoryListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *  远程调用接口 user
 *
 * @author Programmer
 * @date 2026-02-04 13:50
 */
@FeignClient(
        name = "meteor-user",
        contextId = "userPreferenceClient",
        url = "${meteor.remote.user-base-url}"
)
public interface UserPreferenceClient {

    @GetMapping("/internal/user/preference")
    UserPreferenceCategoryListDTO getUserPreference(@RequestParam("userId") Long userId);
}
package com.meteor.merchant.client;

import com.meteor.common.dto.UserProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Programmer
 * @date 2026-02-01 11:33
 */
@FeignClient(name="meteor-user")
public interface UserClient {
    @GetMapping("/internal/user/profile")
    UserProfileDTO getProfile(@RequestParam("userId") Long userId);
}

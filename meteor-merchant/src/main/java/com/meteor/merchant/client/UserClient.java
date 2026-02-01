package com.meteor.merchant.client;

import com.meteor.common.dto.UserProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *  用户服务客户端
 *
 * @author Programmer
 * @date 2026-02-01 11:33
 */
@FeignClient(name="meteor-user")
public interface UserClient {
    /**
     *  获取用户信息
     *
     *  @param userId 用户ID
     *  @return 用户信息
     * */
    @GetMapping("/internal/user/profile")
    UserProfileDTO getProfile(@RequestParam("userId") Long userId);
}

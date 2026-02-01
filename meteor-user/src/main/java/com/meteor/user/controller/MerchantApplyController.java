package com.meteor.user.controller;

import com.meteor.common.result.Result;
import com.meteor.satoken.context.LoginContext;
import com.meteor.user.controller.dto.MerchantApplyDTO;
import com.meteor.user.service.IMerchantApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  商家申请接口
 *
 * @author Programmer
 * @date 2026-01-28 13:36
 */
@RestController
@RequestMapping("/user/merchant")
@Slf4j
@Tag(name = "商家申请", description = "用户侧商家申请接口")
@RequiredArgsConstructor
public class MerchantApplyController {

    private final IMerchantApplyService merchantApplyService;
    private final LoginContext loginContext;

    @Operation(summary = "申请成为商户", description = "用户申请成为商户")
    @PostMapping("/apply")
    public Result<Void> applyMerchant(@RequestBody @Valid MerchantApplyDTO dto) {

        Long userId = loginContext.currentLoginId();

        merchantApplyService.apply(userId, dto);

        return Result.success();
    }
}

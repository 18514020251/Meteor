package com.meteor.merchant.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.meteor.common.result.Result;
import com.meteor.merchant.controller.dto.UpdateShopNoticeDTO;
import com.meteor.merchant.controller.vo.MerchantMeVO;
import com.meteor.merchant.service.IMerchantService;
import com.meteor.satoken.constants.RoleConst;
import com.meteor.satoken.context.LoginContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商家表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-01-31
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/merchant")
@SaCheckRole(RoleConst.MERCHANT)
public class MerchantController {

    private final IMerchantService merchantService;
    private final LoginContext loginContext;

    @PutMapping("/updateNotice")
    @Operation(summary = "修改商家公告")
    public Result<Void> updateShopNotice(@RequestBody @Valid UpdateShopNoticeDTO dto) {
        Long uid = loginContext.currentLoginId();
        merchantService.updateShopNotice(uid, dto.getNotice());
        return Result.success();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户的信息")
    public Result<MerchantMeVO> getMe() {
        Long userId = loginContext.currentLoginId();

        MerchantMeVO merchantMeVO = merchantService.getMe(userId);

        return Result.success(merchantMeVO);
    }

    @DeleteMapping("/me")
    @Operation(summary = "注销当前商家")
    public Result<Void> deleteMe() {
        Long userId = loginContext.currentLoginId();

        merchantService.deleteByMerchantId(userId);

        return Result.success();
    }

}

package com.meteor.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meteor.admin.controller.dto.MerchantApplyUnsentQueryDTO;
import com.meteor.admin.controller.dto.ReviewedResendDTO;
import com.meteor.admin.controller.vo.MerchantApplyUnsentVO;
import com.meteor.admin.controller.vo.ReviewedResendVO;
import com.meteor.admin.service.IMerchantApplyReviewedCompensateService;
import com.meteor.admin.service.IMerchantApplyReviewedQueryService;
import com.meteor.common.result.Result;
import com.meteor.satoken.constants.RoleConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
// NOTE：后续做接口，查询所有在线用户/踢出用户在线状态
/**
 *  商家申请审核AdminController
 *
 * @author Programmer
 * @date 2026-01-29 12:10
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "商家申请审核", description = "管理端商家申请审核接口")
@RequestMapping("/admin/merchant-apply/reviewed")
@SaCheckRole(RoleConst.ADMIN)
public class MerchantApplyReviewedAdminController {

    private final IMerchantApplyReviewedQueryService queryService;
    private final IMerchantApplyReviewedCompensateService compensateService;

    @Operation(summary = "未发送的商家申请列表")
    @GetMapping("/unsent")
    public Result<IPage<MerchantApplyUnsentVO>> unsent(@ModelAttribute MerchantApplyUnsentQueryDTO dto) {
        return Result.success(queryService.listUnsent(dto));
    }

    @Operation(summary = "单条补发审核结果消息")
    @PostMapping("/resend")
    public Result<ReviewedResendVO> resend(@Valid @RequestBody ReviewedResendDTO dto) {
        return Result.success(compensateService.resendByApplyId(dto.getApplyId()));
    }
}

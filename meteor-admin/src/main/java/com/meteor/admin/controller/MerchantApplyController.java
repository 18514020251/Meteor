package com.meteor.admin.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.domain.dto.MerchantApplyRejectDTO;
import com.meteor.admin.service.IMerchantApplyService;
import com.meteor.common.domain.PageResult;
import com.meteor.common.result.Result;
import com.meteor.satoken.constants.RoleConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商家申请表（管理端，审核视图） 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-01-23
 */
// NOTE:系统消息接口
@Slf4j
@RestController
@RequestMapping("/admin/merchant-apply")
@RequiredArgsConstructor
@Tag(name = "商家审核", description = "管理端商家申请审核接口")
@SaCheckRole(RoleConst.ADMIN)
public class MerchantApplyController {

    private final IMerchantApplyService merchantApplyService;

    @Operation(summary = "分页查询商家申请列表")
    @GetMapping
    public Result<PageResult<MerchantApplyDTO>> list(MerchantApplyQueryDTO query) {
        return Result.success(merchantApplyService.list(query));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/{applyId}/approve")
    public Result<Void> approve(@PathVariable Long applyId) {
        merchantApplyService.approveByApplyId(applyId);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/{applyId}/reject")
    public Result<Void> reject(@PathVariable Long applyId,
                               @RequestBody @Valid MerchantApplyRejectDTO req) {
        merchantApplyService.rejectByApplyId(applyId, req.getRejectReason());
        return Result.success();
    }
}

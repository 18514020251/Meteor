package com.meteor.admin.controller;


import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.service.IMerchantApplyService;
import com.meteor.common.domain.PageResult;
import com.meteor.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商家申请表（管理端，审核视图） 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-01-23
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class MerchantApplyController {

    private final IMerchantApplyService merchantApplyService;

    @Operation(summary = "分页查询商家申请列表")
    @GetMapping("/list")
    public PageResult<MerchantApplyDTO> list(MerchantApplyQueryDTO query) {
        return merchantApplyService.list(query);
    }

    @Operation(summary = "审核通过")
    @PostMapping("/merchant-apply/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        merchantApplyService.approve(id);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/merchant-apply/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectReq req) {
        merchantApplyService.reject(id, req.getRejectReason());
        return Result.success();
    }

    @Data
    public static class RejectReq {
        @NotBlank(message = "拒绝原因不能为空")
        private String rejectReason;
    }

}

package com.meteor.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meteor.admin.domain.dto.MerchantApplyUnsentQueryDTO;
import com.meteor.admin.domain.vo.MerchantApplyUnsentVO;
import com.meteor.admin.service.IMerchantApplyReviewedQueryService;
import com.meteor.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class MerchantApplyReviewedAdminController {

    private final IMerchantApplyReviewedQueryService queryService;

    @Operation(summary = "未发送的商家申请列表")
    @GetMapping("/unsent")
    public Result<IPage<MerchantApplyUnsentVO>> unsent(MerchantApplyUnsentQueryDTO dto) {
        return Result.success(queryService.listUnsent(dto));
    }
}

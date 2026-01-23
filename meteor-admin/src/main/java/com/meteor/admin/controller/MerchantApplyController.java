package com.meteor.admin.controller;


import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.service.IMerchantApplyService;
import com.meteor.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/list")
    public PageResult<MerchantApplyDTO> list(MerchantApplyQueryDTO query) {
        return merchantApplyService.list(query);
    }

}

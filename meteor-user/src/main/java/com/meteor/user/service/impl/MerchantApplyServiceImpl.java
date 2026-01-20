package com.meteor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.common.exception.BizException;
import com.meteor.user.domain.dto.MerchantApplyDTO;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.domain.merchant.enums.MerchantApplyStatusEnum;
import com.meteor.user.mapper.MerchantApplyMapper;
import com.meteor.user.service.IMerchantApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.meteor.common.exception.CommonErrorCode.OPERATION_NOT_ALLOWED;

/**
 * @author Programmer
 * @date 2026-01-20 23:36
 */
@Service
@RequiredArgsConstructor
public class MerchantApplyServiceImpl implements IMerchantApplyService {

    private final MerchantApplyMapper merchantApplyMapper;

    @Override
    // todo: 名字重复无法重复添加
    public void apply(Long userId, MerchantApplyDTO applyReason) {

        boolean exists = merchantApplyMapper.exists(
                new LambdaQueryWrapper<MerchantApply>()
                        .eq(MerchantApply::getUserId, userId)
                        .eq(MerchantApply::getStatus, MerchantApplyStatusEnum.PENDING.getCode())
        );

        if (exists) {
            throw new BizException(OPERATION_NOT_ALLOWED);
        }


        MerchantApply apply = MerchantApply.pending(userId, applyReason);

        merchantApplyMapper.insert(apply);


    }
}

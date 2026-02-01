package com.meteor.merchant.service.impl;

import com.meteor.common.dto.UserProfileDTO;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.merchant.client.UserClient;
import com.meteor.merchant.controller.vo.MerchantMeVO;
import com.meteor.merchant.domain.entity.Merchant;
import com.meteor.merchant.mapper.MerchantMapper;
import com.meteor.merchant.service.IMerchantService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.merchant.service.assembler.MerchantMeAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 商家表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-31
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements IMerchantService {

    private final UserClient userClient;
    private final MerchantMeAssembler merchantMeAssembler;

    @Override
    public void updateShopNotice(Long userId, String notice) {
        Merchant merchant = lambdaQuery()
                .eq(Merchant::getUserId, userId)
                .one();

        if (merchant == null) {
            throw new BizException(CommonErrorCode.MERCHANT_NOT_EXIST);
        }

        if (!merchant.canEditProfile()) {
            throw new BizException(CommonErrorCode.MERCHANT_STATUS_INVALID);
        }

        merchant.setNotice(notice);
        merchant.setUpdateTime(LocalDateTime.now());

        updateById(merchant);
    }

    @Override
    public MerchantMeVO getMe(Long userId) {
        Merchant merchant = lambdaQuery()
                .eq(Merchant::getUserId, userId)
                .one();

        if (merchant == null) {
            throw new BizException(CommonErrorCode.MERCHANT_NOT_EXIST);
        }

        UserProfileDTO userProfile = userClient.getProfile(userId);

        if (userProfile == null) {
            throw new BizException(CommonErrorCode.USER_NOT_EXIST);
        }

        return merchantMeAssembler.toVO(merchant, userProfile);
    }
}

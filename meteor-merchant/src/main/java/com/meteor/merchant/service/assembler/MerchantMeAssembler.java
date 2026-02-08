package com.meteor.merchant.service.assembler;

import com.meteor.api.contract.user.dto.UserProfileDTO;
import com.meteor.merchant.controller.vo.MerchantMeVO;
import com.meteor.merchant.domain.entity.Merchant;
import org.springframework.stereotype.Component;

/**
 *  商家自身信息 assembler
 *
 * @author Programmer
 * @date 2026-02-01 11:52
 */
@Component
public class MerchantMeAssembler {
    public MerchantMeVO toVO(Merchant merchant, UserProfileDTO profile) {
        return new MerchantMeVO(
                merchant.getId(),
                merchant.getUserId(),
                merchant.getShopName(),
                merchant.getNotice(),
                merchant.getStatus(),
                merchant.getVerifiedTime(),
                profile != null ? profile.getUsername() : null,
                profile != null ? profile.getPhone() : null,
                profile != null ? profile.getAvatar() : null
        );
    }
}

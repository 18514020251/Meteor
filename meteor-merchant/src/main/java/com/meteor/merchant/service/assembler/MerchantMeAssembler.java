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
        MerchantMeVO vo = new MerchantMeVO();
        vo.setMerchantId(merchant.getId());
        vo.setUserId(merchant.getUserId());
        vo.setShopName(merchant.getShopName());
        vo.setNotice(merchant.getNotice());
        vo.setStatus(merchant.getStatus());
        vo.setVerifiedTime(merchant.getVerifiedTime());

        if (profile != null) {
            vo.setUsername(profile.getUsername());
            vo.setPhone(profile.getPhone());
            vo.setAvatar(profile.getAvatar());
        }
        return vo;
    }
}

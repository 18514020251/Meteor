package com.meteor.merchant.service;

import com.meteor.merchant.controller.vo.MerchantMeVO;
import com.meteor.merchant.domain.entity.Merchant;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商家表 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-31
 */
public interface IMerchantService extends IService<Merchant> {
    /**
     * 修改商家公告
     *
     * @param merchantId 商家ID
     * @param notice 公告
     * */
    void updateShopNotice(Long merchantId, String notice);

    /**
     * 获取当前登录用户的信息
     *
     * @param userId 用户ID
     * @return 商家信息
     * */
    MerchantMeVO getMe(Long userId);
}

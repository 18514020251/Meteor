package com.meteor.merchant.service.impl;

import com.meteor.merchant.domain.entity.Merchant;
import com.meteor.merchant.mapper.MerchantMapper;
import com.meteor.merchant.service.IMerchantService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商家表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-31
 */
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements IMerchantService {

}

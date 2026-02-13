package com.meteor.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meteor.api.model.MerchantMqFailureEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 *  创建失败消息
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-12 18:13
 */
@Mapper
public interface MerchantMqFailMsgMapper extends BaseMapper<MerchantMqFailureEntity> {

}

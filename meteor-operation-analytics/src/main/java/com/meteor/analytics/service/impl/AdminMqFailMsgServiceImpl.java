package com.meteor.analytics.service.impl;

import com.meteor.analytics.domain.entity.AdminMqFailMsg;
import com.meteor.analytics.mapper.AdminMqFailMsgMapper;
import com.meteor.analytics.service.IAdminMqFailMsgService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 管理端-MQ失败/待补发表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
@Service
public class AdminMqFailMsgServiceImpl extends ServiceImpl<AdminMqFailMsgMapper, AdminMqFailMsg> implements IAdminMqFailMsgService {

}

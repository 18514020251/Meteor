package com.meteor.message.service.impl;

import com.meteor.message.domain.entity.SysMessage;
import com.meteor.message.mapper.SysMessageMapper;
import com.meteor.message.service.ISysMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统消息表(公告/系统通知内容源) 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-29
 */
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements ISysMessageService {

}

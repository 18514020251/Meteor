package com.meteor.message.service;

import com.meteor.common.domain.PageResult;
import com.meteor.message.domain.dto.GetTheMessageDTO;
import com.meteor.message.domain.entity.UserMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.message.domain.vo.UserMessageVO;

/**
 * <p>
 * 用户消息表(收件箱，记录已读/删除状态) 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-29
 */
public interface IUserMessageService extends IService<UserMessage> {

    /**
     * 获取收件箱列表
     *
     * @param getTheMessageDTO
     * @return 查询返回结果
     */
    PageResult<UserMessageVO> pageInbox(GetTheMessageDTO getTheMessageDTO , Long userId);
}

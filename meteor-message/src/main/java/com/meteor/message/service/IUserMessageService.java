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
     * @return 查询返回结果
     */
    PageResult<UserMessageVO> pageInbox(GetTheMessageDTO getTheMessageDTO , Long userId);

    /**
     *  标记已读
     *
     *  @param id 消息ID
     *  @param userId 用户ID
     * */
    void markRead(Long id, Long userId);
    /**
     * 一键已读所有
     *
     * @param userId 用户ID
     * @return 影响行数
     * */
    int markReadAll(Long userId);

    /**
     * 删除单个
     *
     * @param id 消息ID
     * @param userId 用户ID
     * */
    void deleteOne(Long id, Long userId);
}
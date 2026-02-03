package com.meteor.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.common.domain.PageResult;
import com.meteor.message.controller.dto.GetTheMessageDTO;
import com.meteor.message.controller.vo.UserMessageVO;
import com.meteor.message.domain.entity.UserMessage;

/**
 * 用户消息服务接口。
 * <p>
 * 用于处理用户收件箱中的消息查询、已读状态变更及删除操作，
 * 支持单条和批量处理。
 * </p>
 *
 * @author Programmer
 * @since 2026-01-29
 */
public interface IUserMessageService extends IService<UserMessage> {

    /**
     * 分页查询用户收件箱消息列表。
     * <p>
     * 根据查询条件获取当前用户的消息列表，
     * 默认仅返回未被逻辑删除的消息。
     * </p>
     *
     * @param getTheMessageDTO 查询条件（分页参数、筛选条件等）
     * @param userId 当前用户 ID
     * @return 分页后的用户消息列表
     */
    PageResult<UserMessageVO> pageInbox(GetTheMessageDTO getTheMessageDTO, Long userId);

    /**
     * 将指定消息标记为已读。
     * <p>
     * 仅允许操作属于当前用户的消息，
     * 若消息不存在或不属于该用户，将抛出业务异常。
     * </p>
     *
     * @param id 消息 ID
     * @param userId 当前用户 ID
     */
    void markRead(Long id, Long userId);

    /**
     * 将当前用户的所有未读消息标记为已读。
     *
     * @param userId 当前用户 ID
     * @return 实际更新的消息数量
     */
    int markReadAll(Long userId);

    /**
     * 删除指定的单条消息。
     * <p>
     * 该操作为逻辑删除，仅影响当前用户的消息记录。
     * </p>
     *
     * @param id 消息 ID
     * @param userId 当前用户 ID
     */
    void deleteOne(Long id, Long userId);

    /**
     * 删除当前用户的所有消息。
     * <p>
     * 该操作为批量逻辑删除，不会物理移除数据。
     * </p>
     *
     * @param userId 当前用户 ID
     * @return 实际删除的消息数量
     */
    int deleteAll(Long userId);

    /**
     *  查询未读消息数量
     *
     *  @param userId 用户ID
     *  @return 未读消息数
     * */
    long getUnreadCount(Long userId);
}

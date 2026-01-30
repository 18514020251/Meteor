package com.meteor.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meteor.common.domain.PageResult;
import com.meteor.common.enums.DeleteStatus;
import com.meteor.common.enums.message.MessageReadStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.message.domain.assembler.UserMessageAssembler;
import com.meteor.message.domain.dto.GetTheMessageDTO;
import com.meteor.message.domain.entity.UserMessage;
import com.meteor.message.domain.vo.UserMessageVO;
import com.meteor.message.enums.ReadStatusQueryEnum;
import com.meteor.message.mapper.UserMessageMapper;
import com.meteor.message.service.IUserMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户消息表(收件箱，记录已读/删除状态) 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-29
 */
@Service
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage> implements IUserMessageService {

    @Override
    public PageResult<UserMessageVO> pageInbox(GetTheMessageDTO getTheMessageDTO , Long userId) {
        Integer pageNum = getTheMessageDTO.getPageNum();

        LambdaQueryWrapper<UserMessage> qw = buildMessageQueryWrapper(getTheMessageDTO , userId);

        Page<UserMessage> page = this.page(new Page<>(pageNum, getTheMessageDTO.getPageSize()), qw);

        List<UserMessageVO> list = page.getRecords()
                .stream()
                .map(UserMessageAssembler::toVO)
                .toList();
        return PageResult.of(list, page.getTotal(), page.getCurrent(), page.getSize());

    }

    private LambdaQueryWrapper<UserMessage> buildMessageQueryWrapper(GetTheMessageDTO dto, Long userId) {
        LambdaQueryWrapper<UserMessage> qw = new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getDeleted, DeleteStatus.NORMAL.getCode())
                .orderByDesc(UserMessage::getId);

        ReadStatusQueryEnum q = ReadStatusQueryEnum.fromCode(dto.getReadStatus());
        if (q == ReadStatusQueryEnum.UNREAD) {
            qw.eq(UserMessage::getReadStatus, ReadStatusQueryEnum.UNREAD.getCode());
        } else if (q == ReadStatusQueryEnum.READ) {
            qw.eq(UserMessage::getReadStatus, ReadStatusQueryEnum.READ.getCode());
        }

        qw.select(
                UserMessage::getId,
                UserMessage::getType,
                UserMessage::getTitle,
                UserMessage::getContent,
                UserMessage::getReadStatus,
                UserMessage::getCreateTime,
                UserMessage::getReadTime
        );
        return qw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id, Long userId) {

        if (updateUnreadToRead(id, userId)) {
            return;
        }

        UserMessage msg = checkMessageExist(id, userId);

        if (MessageReadStatusEnum.READ.equals(msg.getReadStatus())) {
            return;
        }

        throw new BizException(CommonErrorCode.OPERATION_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markReadAll(Long userId) {
        return updateAllUnreadToRead(userId);
    }

    @Override
    public void deleteOne(Long id, Long userId) {

        if (softDeleteById(id, userId)) {
            return;
        }

        UserMessage msg = findUserMessageByIdAndUserId(id, userId);

        if (msg == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }

        if (DeleteStatus.DELETED.equals(msg.getDeleted())) {
            return;
        }

        throw new BizException(CommonErrorCode.OPERATION_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAll(Long userId) {
        return this.baseMapper.update(null,
                new LambdaUpdateWrapper<UserMessage>()
                        .set(UserMessage::getDeleted, DeleteStatus.DELETED.getCode())
                        .eq(UserMessage::getUserId, userId)
                        .eq(UserMessage::getDeleted, DeleteStatus.NORMAL.getCode())
        );
    }


    /**
     * 根据ID和用户ID查找消息
     */
    private UserMessage findUserMessageByIdAndUserId(Long id, Long userId) {
        return this.lambdaQuery()
                .select(UserMessage::getId, UserMessage::getDeleted)
                .eq(UserMessage::getId, id)
                .eq(UserMessage::getUserId, userId)
                .one();
    }


    /**
     *  逻辑删除消息
     * */
    private boolean softDeleteById(Long id, Long userId) {
        return this.lambdaUpdate()
                .eq(UserMessage::getId, id)
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getDeleted, DeleteStatus.NORMAL.getCode())
                .set(UserMessage::getDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    /**
     * 一键将未读更新为已读
     */
    private int updateAllUnreadToRead(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        UserMessage entity = new UserMessage();
        entity.setReadStatus(MessageReadStatusEnum.READ);
        entity.setReadTime(now);

        return this.baseMapper.update(entity,
                new LambdaQueryWrapper<UserMessage>()
                        .eq(UserMessage::getUserId, userId)
                        .eq(UserMessage::getDeleted, DeleteStatus.NORMAL.getCode())
                        .eq(UserMessage::getReadStatus, ReadStatusQueryEnum.UNREAD.getCode())
        );
    }

    /**
    *  查询未读消息，并更新为已读
    * */
    private boolean updateUnreadToRead(Long id, Long userId) {
        return this.lambdaUpdate()
                .eq(UserMessage::getId, id)
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getDeleted, DeleteStatus.NORMAL.getCode())
                .eq(UserMessage::getReadStatus, ReadStatusQueryEnum.UNREAD.getCode())
                .set(UserMessage::getReadStatus, ReadStatusQueryEnum.READ.getCode())
                .set(UserMessage::getReadTime, LocalDateTime.now())
                .update();
    }


    /**
     *  检查消息是否存在
     * */
    private UserMessage checkMessageExist(Long id, Long userId) {
        UserMessage msg = this.lambdaQuery()
                .select(UserMessage::getId,
                        UserMessage::getReadStatus,
                        UserMessage::getDeleted)
                .eq(UserMessage::getId, id)
                .eq(UserMessage::getUserId, userId)
                .one();

        if (msg == null || DeleteStatus.DELETED.equals(msg.getDeleted())) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }

        return msg;
    }
}

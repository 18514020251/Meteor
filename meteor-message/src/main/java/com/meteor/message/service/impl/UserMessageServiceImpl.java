package com.meteor.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meteor.common.domain.PageResult;
import com.meteor.common.enums.DeleteStatus;
import com.meteor.message.domain.assembler.UserMessageAssembler;
import com.meteor.message.domain.dto.GetTheMessageDTO;
import com.meteor.message.domain.entity.UserMessage;
import com.meteor.message.domain.vo.UserMessageVO;
import com.meteor.message.enums.ReadStatusQueryEnum;
import com.meteor.message.mapper.UserMessageMapper;
import com.meteor.message.service.IUserMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
}

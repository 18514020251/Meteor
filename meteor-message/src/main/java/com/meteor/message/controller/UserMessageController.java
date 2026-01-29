package com.meteor.message.controller;


import com.meteor.common.domain.PageResult;
import com.meteor.common.result.Result;
import com.meteor.message.domain.dto.GetTheMessageDTO;
import com.meteor.message.domain.vo.UserMessageVO;
import com.meteor.message.service.IUserMessageService;
import com.meteor.satoken.context.LoginContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户消息表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-01-29
 */
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "用户消息表")
public class UserMessageController {

    private final IUserMessageService userMessageService;
    private final LoginContext loginContext;

    @GetMapping()
    @Operation(summary = "分页查询")
    public Result<PageResult<UserMessageVO>> page(
            @ModelAttribute GetTheMessageDTO getTheMessageDTO
            ){
        Long userId = loginContext.currentLoginId();
        return Result.success(userMessageService.pageInbox(getTheMessageDTO , userId));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "单条消息已读")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = loginContext.currentLoginId();
        userMessageService.markRead(id, userId);
        return Result.success();
    }

}

package com.meteor.message.controller;


import com.meteor.common.domain.PageResult;
import com.meteor.common.result.Result;
import com.meteor.message.controller.dto.GetTheMessageDTO;
import com.meteor.message.controller.vo.UserMessageVO;
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

    @PostMapping("/read-all")
    @Operation(summary = "一键已读")
    public Result<Integer> markReadAll() {
        Long userId = loginContext.currentLoginId();
        int count = userMessageService.markReadAll(userId);
        return Result.success(count);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单条消息删除")
    public Result<Void> deleteOne(@PathVariable Long id) {
        Long userId = loginContext.currentLoginId();
        userMessageService.deleteOne(id, userId);
        return Result.success();
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "一键删除全部消息")
    public Result<Integer> deleteAll() {
        Long userId = loginContext.currentLoginId();
        int count = userMessageService.deleteAll(userId);
        return Result.success(count);
    }


}

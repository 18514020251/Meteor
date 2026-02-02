package com.meteor.movie.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.meteor.common.result.Result;
import com.meteor.movie.controller.vo.MediaUploadVO;
import com.meteor.movie.enums.MediaAssetKindEnum;
import com.meteor.movie.service.IMediaAssetService;
import com.meteor.satoken.constants.RoleConst;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 图片资源表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@RestController
@RequestMapping("/movies/pic")
@Tag(name = "图片资源表")
@RequiredArgsConstructor
public class MediaAssetController {

    private final IMediaAssetService mediaUploadService;

    @PostMapping("/media")
    @SaCheckRole(RoleConst.MERCHANT)
    public Result<MediaUploadVO> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("kind") MediaAssetKindEnum kind
    ) {
        MediaUploadVO vo = mediaUploadService.upload(file, kind);
        return Result.success(vo);
    }


}

package com.meteor.movie.service.impl;

import com.meteor.common.constants.MediaConstants;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.minio.util.MinioUtil;
import com.meteor.movie.controller.vo.MediaUploadVO;
import com.meteor.movie.domain.entity.MediaAsset;
import com.meteor.movie.enums.MediaAssetKindEnum;
import com.meteor.movie.mapper.MediaAssetMapper;
import com.meteor.movie.service.IMediaAssetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.movie.service.assembler.MediaAssetAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 图片资源表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaAssetServiceImpl extends ServiceImpl<MediaAssetMapper, MediaAsset> implements IMediaAssetService {

    private final MediaAssetAssembler assembler;
    private final MinioUtil minioUtil;

    @Override
    public void createForMovie(
            Long movieId,
            String posterKey,
            String coverKey,
            List<String> galleryKeys,
            Long operatorId,
            LocalDateTime now
    ) {
        log.info("createForMovie assets: posterKey={}, coverKey={}, gallerySize={}",
                posterKey, coverKey,
                galleryKeys == null ? null : galleryKeys.size());

        if (hasText(posterKey)) {
            baseMapper.insert(
                    assembler.createForMovie(
                            movieId,
                            posterKey.trim(),
                            MediaAssetKindEnum.POSTER,
                            0,
                            operatorId,
                            now
                    )
            );
        }

        // cover
        if (hasText(coverKey)) {
            baseMapper.insert(
                    assembler.createForMovie(
                            movieId,
                            coverKey.trim(),
                            MediaAssetKindEnum.COVER,
                            0,
                            operatorId,
                            now
                    )
            );
        }

        // gallery
        if (galleryKeys == null || galleryKeys.isEmpty()) {
            return;
        }

        int sort = 0;
        for (String key : galleryKeys) {
            if (!hasText(key)) {
                continue;
            }
            baseMapper.insert(
                    assembler.createForMovie(
                            movieId,
                            key.trim(),
                            MediaAssetKindEnum.GALLERY,
                            sort++,
                            operatorId,
                            now
                    )
            );
        }
    }

    @Override
    public MediaUploadVO upload(MultipartFile file, MediaAssetKindEnum kind) {

        if (file == null || file.isEmpty()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        String contentType = file.getContentType();
        if (contentType == null || !MediaConstants.ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BizException(CommonErrorCode.IMAGE_TYPE_ERROR);
        }

        if (file.getSize() > MediaConstants.MAX_IMAGE_SIZE) {
            throw new BizException(CommonErrorCode.FILE_SIZE_ERROR);
        }

        if (kind == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        String objectKey = buildObjectKey(file, kind);

        try (InputStream in = file.getInputStream()) {
            minioUtil.putObject(
                    objectKey,
                    in,
                    contentType
            );
        } catch (IOException e) {
            throw new BizException(CommonErrorCode.FILE_UPLOAD_FAILED);
        }

        String previewUrl = minioUtil.buildPresignedUrl(objectKey);

        MediaUploadVO vo = new MediaUploadVO();
        vo.setObjectKey(objectKey);
        vo.setPreviewUrl(previewUrl);
        return vo;
    }


    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String buildObjectKey(MultipartFile file, MediaAssetKindEnum kind) {

        String ext = getFileExtension(file);
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String uuid = UUID.randomUUID().toString().replace("-", "");

        return String.format(
                "movie/%s/%s/%s.%s",
                kind.getDir(),
                date,
                uuid,
                ext
        );
    }

    private String getFileExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || !original.contains(".")) {
            throw new BizException(CommonErrorCode.IMAGE_TYPE_ERROR);
        }
        return original.substring(original.lastIndexOf('.') + 1).toLowerCase();
    }

}

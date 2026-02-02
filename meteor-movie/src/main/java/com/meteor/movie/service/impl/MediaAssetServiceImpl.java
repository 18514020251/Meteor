package com.meteor.movie.service.impl;

import com.meteor.movie.domain.entity.MediaAsset;
import com.meteor.movie.enums.MediaAssetKindEnum;
import com.meteor.movie.mapper.MediaAssetMapper;
import com.meteor.movie.service.IMediaAssetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.movie.service.assembler.MediaAssetAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
public class MediaAssetServiceImpl extends ServiceImpl<MediaAssetMapper, MediaAsset> implements IMediaAssetService {


    private final MediaAssetAssembler assembler;

    @Override
    public void createForMovie(
            Long movieId,
            String posterUrl,
            String coverUrl,
            List<String> galleryUrls,
            Long operatorId,
            LocalDateTime now
    ) {
        if (hasText(posterUrl)) {
            baseMapper.insert(
                    assembler.createForMovie(
                            movieId,
                            posterUrl,
                            MediaAssetKindEnum.POSTER,
                            0,
                            operatorId,
                            now
                    )
            );
        }

        if (hasText(coverUrl)) {
            baseMapper.insert(
                    assembler.createForMovie(
                            movieId,
                            coverUrl,
                            MediaAssetKindEnum.COVER,
                            0,
                            operatorId,
                            now
                    )
            );
        }

        if (galleryUrls == null || galleryUrls.isEmpty()) {
            return;
        }

        int sort = 0;
        for (String url : galleryUrls) {
            if (!hasText(url)) {
                continue;
            }
            baseMapper.insert(
                    assembler.createForMovie(
                            movieId,
                            url.trim(),
                            MediaAssetKindEnum.GALLERY,
                            sort++,
                            operatorId,
                            now
                    )
            );
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}

package com.meteor.movie.service;

import com.meteor.movie.domain.entity.MediaAsset;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 图片资源表 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface IMediaAssetService extends IService<MediaAsset> {

    /**
     *  创建
     * */
    void createForMovie(
            Long movieId,
            String posterUrl,
            String coverUrl,
            List<String> galleryUrls,
            Long operatorId,
            LocalDateTime now
    );
}

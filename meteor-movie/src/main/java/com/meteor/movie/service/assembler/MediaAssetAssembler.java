package com.meteor.movie.service.assembler;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.movie.domain.entity.MediaAsset;
import com.meteor.movie.enums.MediaAssetKindEnum;
import com.meteor.movie.enums.MediaBizTypeEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *  MediaAsset 构造器
 *
 * @author Programmer
 * @date 2026-02-02 17:20
 */
@Component
public class MediaAssetAssembler {

    private MediaAssetAssembler() {}

    public MediaAsset createForMovie(
            Long movieId,
            String objecyKey,
            MediaAssetKindEnum kind,
            int sort,
            Long operatorId,
            LocalDateTime now
    ) {
        MediaAsset asset = new MediaAsset();
        asset.setBizType(MediaBizTypeEnum.MOVIE);
        asset.setBizId(movieId);
        asset.setObjectKey(objecyKey);
        asset.setKind(kind);
        asset.setSort(sort);
        asset.setCreateTime(now);
        asset.setUpdateTime(now);
        asset.setCreateBy(operatorId);
        asset.setUpdateBy(operatorId);
        asset.setDeleted(DeleteStatus.NORMAL);
        return asset;
    }
}

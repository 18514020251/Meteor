package com.meteor.movie.mapper;

import com.meteor.movie.domain.entity.MediaAsset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 图片资源表 Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface MediaAssetMapper extends BaseMapper<MediaAsset> {

    /**
     * 判断 objectKey 是否仍被使用（未删除）
     */
    @Select("""
        select 1
        from media_asset
        where object_key = #{objectKey}
          and deleted = 0
        limit 1
        """)
    Integer existsByObjectKey(String objectKey);
}

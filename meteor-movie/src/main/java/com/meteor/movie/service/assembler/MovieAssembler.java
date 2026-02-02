package com.meteor.movie.service.assembler;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.movie.controller.dto.MovieCreateDTO;
import com.meteor.movie.domain.entity.Movie;
import com.meteor.movie.enums.MovieStatusEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Movie 构造器
 *
 * @author Programmer
 * @date 2026-02-02 16:55
 */
@Component
public class MovieAssembler {

    private MovieAssembler() {}

    public Movie create(MovieCreateDTO dto, Long operatorId, LocalDateTime now) {
        return new Movie()
                .setTitle(dto.getTitle())
                .setAlias(dto.getAlias())
                .setIntro(dto.getIntro())
                .setDurationMin(dto.getDurationMin())
                .setReleaseDate(dto.getReleaseDate())
                .setStatus(MovieStatusEnum.COMING)
                .setCreateTime(now)
                .setUpdateTime(now)
                .setCreateBy(operatorId)
                .setUpdateBy(operatorId)
                .setDeleted(DeleteStatus.NORMAL);
    }
}

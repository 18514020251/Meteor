package com.meteor.movie.service.assembler;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.movie.domain.entity.MovieCategoryRel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Programmer
 * @date 2026-02-02 17:08
 */
@Component
public class MovieCategoryRelAssembler {

    private MovieCategoryRelAssembler() {}

    public MovieCategoryRel create(
            Long movieId,
            Long categoryId,
            Long operatorId,
            LocalDateTime now
    ) {
        MovieCategoryRel rel = new MovieCategoryRel();
        rel.setMovieId(movieId);
        rel.setCategoryId(categoryId);
        rel.setCreateTime(now);
        rel.setUpdateTime(now);
        rel.setCreateBy(operatorId);
        rel.setUpdateBy(operatorId);
        rel.setDeleted(DeleteStatus.NORMAL);
        return rel;
    }
}

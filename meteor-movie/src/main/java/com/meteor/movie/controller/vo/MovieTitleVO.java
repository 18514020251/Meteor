package com.meteor.movie.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *  电影标题 VO
 *
 * @author Programmer
 * @date 2026-02-02 19:26
 */
@Data
@AllArgsConstructor
public class MovieTitleVO {
    private Long id;
    private String title;
}


package com.meteor.movie.controller.vo;


/**
 *  电影标题 VO
 *
 * @author Programmer
 * @date 2026-02-02 19:26
 */
public record MovieTitleVO(String id, String title) {
    public MovieTitleVO(Long id, String title) {
        this(id != null ? id.toString() : null, title);
    }
}


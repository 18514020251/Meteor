package com.meteor.movie.controller.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 *  电影搜索参数
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-14 15:33
 */
@Data
public class MovieSearchDTO {

    /**
     * 关键词：title/alias/intro 模糊匹配
     */
    private String q;

    /**
     * 分类筛选（任意命中 OR 语义）
     */
    private List<Long> categoryIds;

    /**
     * 电影状态：1=COMING 2=SHOWING 3=OFF
     */
    private Integer status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releaseFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releaseTo;

    /**
     * 排序：releaseDateDesc / createTimeDesc
     */
    private String sort;

    /**
     * 分页
     */
    private Long page;

    private Long size;
}

package com.community.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminArticleVO {
    private Long id;
    private String title;
    private String authorName;     // 作者昵称（后台需要）
    private Integer status;        // 0 草稿 / 1 已发布
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private LocalDateTime createTime;
}
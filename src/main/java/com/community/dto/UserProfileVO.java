package com.community.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 个人中心资料 */
@Data
public class UserProfileVO {
    private Long id;
    private String nickname;
    private String avatar;
    private String bio;
    private LocalDateTime createTime;
    private Long articleCount;      // 我的文章数
    private Long favoriteCount;     // 我的收藏数
    private Long commentCount;      // 我的评论数
    private Long likeReceivedCount; // 我收到的赞数
}

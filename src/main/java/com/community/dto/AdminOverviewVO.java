package com.community.dto;

import lombok.Data;

/** 后台统计面板 */
@Data
public class AdminOverviewVO {
    private Long articleCount;   // 文章总数
    private Long userCount;      // 用户总数
    private Long commentCount;   // 评论总数
    private Long tagCount;       // 标签总数
}

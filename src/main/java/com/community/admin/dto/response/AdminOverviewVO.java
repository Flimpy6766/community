package com.community.admin.dto.response;

/** 后台统计面板 */
public class AdminOverviewVO {
    private Long articleCount;   // 文章总数
    private Long userCount;      // 用户总数
    private Long commentCount;   // 评论总数
    private Long tagCount;       // 标签总数

    public Long getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(Long articleCount) {
        this.articleCount = articleCount;
    }

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getTagCount() {
        return tagCount;
    }

    public void setTagCount(Long tagCount) {
        this.tagCount = tagCount;
    }
}

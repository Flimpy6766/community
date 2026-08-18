package com.community.article.mapper.row;


/** 个人资料页的统计结果。 */
public class UserProfileStats {

    private Long articleCount;
    private Long favoriteCount;
    private Long commentCount;
    private Long likeReceivedCount;

    public Long getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(Long articleCount) {
        this.articleCount = articleCount;
    }

    public Long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getLikeReceivedCount() {
        return likeReceivedCount;
    }

    public void setLikeReceivedCount(Long likeReceivedCount) {
        this.likeReceivedCount = likeReceivedCount;
    }
}

package com.community.user.dto.response;


import java.time.LocalDateTime;

/** 个人中心资料 */
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

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

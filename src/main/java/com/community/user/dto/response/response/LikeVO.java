package com.community.user.dto.response.response;


public class LikeVO {

    /* 当前用户是否点赞 */
    private Boolean liked;

    /* 最新点赞数 */
    private Integer likeCount;

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }
}

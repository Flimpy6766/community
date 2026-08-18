package com.community.user.dto.response.response;

import com.community.article.entity.Article;

import java.util.List;



public class ArticleVO extends Article {
    private List<String> tags;

    /** 当前登录用户是否已点赞（详情接口返回） */
    private Boolean liked;

    /** 当前登录用户是否已收藏（详情接口返回） */
    private Boolean favorited;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getFavorited() {
        return favorited;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }
}

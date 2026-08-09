package com.community.dto;

import com.community.entity.Article;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleVO extends Article {
    private List<String> tags;

    /** 当前登录用户是否已点赞（详情接口返回） */
    private Boolean liked;

    /** 当前登录用户是否已收藏（详情接口返回） */
    private Boolean favorited;
}

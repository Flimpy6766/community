package com.community.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.user.dto.response.response.ArticleVO;
import com.community.user.dto.response.response.CommentVO;


import java.util.List;


public interface ArticleQueryService {
    IPage<ArticleVO> list(Integer page, Integer size);
    IPage<ArticleVO> search(String keyword, Integer page, Integer size);
    IPage<ArticleVO> listMyArticles(Integer status, Integer page, Integer size);
    IPage<ArticleVO> listMyFavorites(Integer page, Integer size);

    // 评论列表
    IPage<CommentVO> listComments(Long articleId, Integer page, Integer size);

    List<ArticleVO> hotList(Integer limit);

}

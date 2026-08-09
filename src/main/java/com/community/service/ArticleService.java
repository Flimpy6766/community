package com.community.service;

import com.community.dto.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface ArticleService {
    Long create(CreateArticleDTO dto);
    void update(Long id, CreateArticleDTO dto);
    void delete(Long id);

    ArticleVO getDetail(Long id);
    IPage<ArticleVO> list(Integer page, Integer size);
    IPage<ArticleVO> search(String keyword, Integer page, Integer size);

    LikeVO toggleLike(Long articleId);

    FavoriteVO toggleFavorite(Long articleId);

    IPage<ArticleVO> listMyArticles(Integer status, Integer page, Integer size);
    IPage<ArticleVO> listMyFavorites(Integer page, Integer size);

    void addComment(Long articleId, CreateCommentDTO dto);
    void deleteComment(Long id);
    IPage<CommentVO> listComments(Long articleId, Integer page, Integer size);

    List<ArticleVO> hotList(Integer limit);
}


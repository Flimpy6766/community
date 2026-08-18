package com.community.article.service;

import com.community.article.dto.request.CreateCommentDTO;
import com.community.user.dto.response.response.FavoriteVO;
import com.community.user.dto.response.response.LikeVO;

public interface ArticleInteractionService {

    LikeVO toggleLike(Long articleId);

    FavoriteVO toggleFavorite(Long articleId);

    void addComment(Long articleId, CreateCommentDTO dto);

    void deleteComment(Long id);
}

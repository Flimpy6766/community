package com.community.article.service;

import com.community.article.dto.request.CreateArticleDTO;
import com.community.user.dto.response.response.ArticleVO;

public interface ArticleService {
    Long create(CreateArticleDTO dto);
    void update(Long id, CreateArticleDTO dto);
    void delete(Long id);
    ArticleVO getDetail(Long id);
}

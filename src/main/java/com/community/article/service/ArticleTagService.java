package com.community.article.service;

import java.util.List;
import java.util.Map;

public interface ArticleTagService {
    void replaceTags(Long articleId, List<String> rawTagNames);
    void deleteByArticleId(Long articleId);
    List<String> findTagNames(Long articleId);
    Map<Long, List<String>> findTagNamesByArticleIds(List<Long> articleIds);
}

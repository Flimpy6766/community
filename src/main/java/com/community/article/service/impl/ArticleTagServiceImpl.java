package com.community.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.article.entity.ArticleTag;
import com.community.article.entity.Tag;
import com.community.article.mapper.ArticleTagMapper;
import com.community.article.mapper.TagMapper;
import com.community.article.mapper.row.ArticleTagNameRow;
import com.community.article.service.ArticleTagService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ArticleTagServiceImpl implements ArticleTagService {

    public ArticleTagServiceImpl(TagMapper tagMapper,
                                 ArticleTagMapper articleTagMapper) {
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
    }

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    /** 批量创建标签并建立文章-标签关系。调用方已处于文章写入事务中。 */
    @Override
    public void replaceTags(Long articleId, List<String> rawTagNames) {
    List<String> tagNames = rawTagNames == null ? Collections.emptyList() : rawTagNames.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(name -> !name.isEmpty())
            .distinct()
            .collect(Collectors.toList());

        if (tagNames.isEmpty()) {
        return;
    }

        tagMapper.insertIgnoreBatch(tagNames);
    List<Tag> tags = tagMapper.selectByNames(tagNames);
    List<ArticleTag> relations = tags.stream()
            .map(tag -> {
                ArticleTag relation = new ArticleTag();
                relation.setArticleId(articleId);
                relation.setTagId(tag.getId());
                return relation;
            })
            .collect(Collectors.toList());
        articleTagMapper.insertBatch(relations);
}

    @Override
    public void deleteByArticleId(Long articleId) {
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        articleTagMapper.delete(wrapper);
    }

    @Override
    public List<String> findTagNames(Long articleId) {
        return articleTagMapper.selectTagNamesByArticleIds(List.of(articleId)).stream()
                .map(ArticleTagNameRow::getTagName)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<String>> findTagNamesByArticleIds(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ArticleTagNameRow> rows =
                articleTagMapper.selectTagNamesByArticleIds(articleIds);

        return rows.stream().collect(Collectors.groupingBy(
                ArticleTagNameRow::getArticleId,
                Collectors.mapping(
                        ArticleTagNameRow::getTagName,
                        Collectors.toList()
                )
        ));
    }
}

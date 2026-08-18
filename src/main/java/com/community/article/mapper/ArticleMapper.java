package com.community.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.article.entity.Article;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    int incrementLikeCount(@Param("articleId") Long articleId,
                           @Param("delta") int delta);

    int incrementFavoriteCount(@Param("articleId") Long articleId,
                               @Param("delta") int delta);

    int incrementCommentCount(@Param("articleId") Long articleId,
                              @Param("delta") int delta);

    int incrementViewCount(@Param("articleId") Long articleId,
                           @Param("delta") long delta);
}

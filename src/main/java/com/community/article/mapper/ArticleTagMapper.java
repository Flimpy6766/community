package com.community.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.article.mapper.row.ArticleTagNameRow;
import com.community.article.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    List<ArticleTagNameRow> selectTagNamesByArticleIds(
            @Param("articleIds") List<Long> articleIds);

    int insertBatch(@Param("relations") List<ArticleTag> relations);
}

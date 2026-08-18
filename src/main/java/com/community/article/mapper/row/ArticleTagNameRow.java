package com.community.article.mapper.row;

/** 文章标签查询的内部结果行。 */
public class ArticleTagNameRow {

    private Long articleId;
    private String tagName;

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}

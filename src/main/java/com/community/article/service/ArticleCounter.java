package com.community.article.service;

import com.community.common.transaction.AfterCommitExecutor;
import com.community.article.entity.Article;
import com.community.article.mapper.ArticleMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 文章计数器：统一管理四种计数（内存同步 + 数据库原子更新）
 */
@Component
public class ArticleCounter {
    public ArticleCounter(ArticleMapper articleMapper, StringRedisTemplate stringRedisTemplate, AfterCommitExecutor afterCommitExecutor, ArticleHotRankService articleHotRankService) {
        this.articleMapper = articleMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.afterCommitExecutor = afterCommitExecutor;
        this.articleHotRankService = articleHotRankService;
    }

    public enum CountType {
        LIKE(2),
        FAVORITE(3),
        COMMENT(4);

        final int weight;      // 热度权重（评论最值钱 > 收藏 > 点赞 > 浏览=1）

        CountType(int weight) {
            this.weight = weight;
        }
    }

    private final ArticleMapper articleMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ArticleHotRankService articleHotRankService;

    private static final String VIEW_KEY_PREFIX = "article:view:";
    public static final String HOT_KEY = "article:hot:rank";


    public void increase(Article article, CountType type, int delta) {
        switch (type) {
            case LIKE -> article.setLikeCount(article.getLikeCount() + delta);
            case FAVORITE -> article.setFavoriteCount(article.getFavoriteCount() + delta);
            case COMMENT -> article.setCommentCount(article.getCommentCount() + delta);
        }

        switch (type) {
            case LIKE -> articleMapper.incrementLikeCount(article.getId(), delta);
            case FAVORITE -> articleMapper.incrementFavoriteCount(article.getId(), delta);
            case COMMENT -> articleMapper.incrementCommentCount(article.getId(), delta);
        }

        // 数据库事务提交后再更新热榜，Redis 不参与数据库回滚。
        afterCommitExecutor.execute(() ->
                articleHotRankService.increment(article.getId(), type.weight * delta));
    }

    public void increaseView(Long articleId) {
        stringRedisTemplate.opsForValue().increment(VIEW_KEY_PREFIX + articleId);
        stringRedisTemplate.opsForZSet().incrementScore(HOT_KEY, String.valueOf(articleId), 1);// 热榜 + 1
    }

    /** 当前未落库的浏览量增量 */
    public long getViewIncrement(Long articleId) {
        String v = stringRedisTemplate.opsForValue().get(VIEW_KEY_PREFIX + articleId);
        return v == null ? 0 : Long.parseLong(v);
    }
}

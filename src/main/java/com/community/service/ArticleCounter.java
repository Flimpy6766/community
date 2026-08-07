package com.community.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.entity.Article;
import com.community.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 文章计数器：统一管理四种计数（内存同步 + 数据库原子更新）
 */
@Component
public class ArticleCounter {

    public enum CountType {
        LIKE("like_count", 2),
        FAVORITE("favorite_count", 3),
        COMMENT("comment_count", 4);

        final String column;   // 数据库列名
        final int weight;      // 热度权重（评论最值钱 > 收藏 > 点赞 > 浏览=1）

        CountType(String column, int weight) {
            this.column = column;
            this.weight = weight;
        }
    }

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String VIEW_KEY_PREFIX = "article:view:";
    public static final String HOT_KEY = "article:hot:rank";


    public void increase(Article article, CountType type, int delta) {
        switch (type) {
            case LIKE -> article.setLikeCount(article.getLikeCount() + delta);
            case FAVORITE -> article.setFavoriteCount(article.getFavoriteCount() + delta);
            case COMMENT -> article.setCommentCount(article.getCommentCount() + delta);
        }

        UpdateWrapper<Article> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", article.getId())
                .setSql(type.column + " = " + type.column + " + " + delta);
        articleMapper.update(null, wrapper);

        // 热榜：按权重加减（点赞+2，取消点赞 delta=-1 → 热度-2；删评论 -N → 热度-4N）
        stringRedisTemplate.opsForZSet().incrementScore(
                HOT_KEY, String.valueOf(article.getId()), type.weight * delta);
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

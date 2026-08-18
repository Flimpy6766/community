package com.community.article.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 文章热榜 Redis 协作者。
 *
 * <p>这里只负责热榜数据，不负责文章数据库计数和业务事务。</p>
 */
@Component
public class ArticleHotRankService {

    public ArticleHotRankService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public static final String HOT_KEY = "article:hot:rank";

    private final StringRedisTemplate stringRedisTemplate;

    public void increment(Long articleId, int delta) {
        stringRedisTemplate.opsForZSet().incrementScore(
                HOT_KEY,
                String.valueOf(articleId),
                delta);
    }

    public void remove(Long articleId) {
        stringRedisTemplate.opsForZSet().remove(
                HOT_KEY,
                String.valueOf(articleId));
    }
}

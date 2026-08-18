package com.community.article.task;

import com.community.article.mapper.ArticleMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/** 浏览量定时落库：把Redis里的增量批量刷进数据库 */
@Component
public class ViewCountSyncTask {

    public ViewCountSyncTask(StringRedisTemplate stringRedisTemplate, ArticleMapper articleMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.articleMapper = articleMapper;
    }

    private static final String VIEW_KEY_PREFIX = "article:view:";
    private static final String DETAIL_CACHE_PREFIX = "article:detail:";

    private final StringRedisTemplate stringRedisTemplate;

    private final ArticleMapper articleMapper;

    /** 每5分钟执行一次 */
    @Scheduled(fixedRate = 300_000)
    public void syncViewCount() {
        // 找出所有有浏览量的文章
        Set<String> keys = stringRedisTemplate.keys(VIEW_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            Long articleId = Long.valueOf(key.substring(VIEW_KEY_PREFIX.length()));

            // 取出增量并删除
            String increment = stringRedisTemplate.opsForValue().getAndDelete(key);
            if (increment == null) {
                continue;
            }

            // 通过 Mapper XML 原子累加数据库浏览量
            articleMapper.incrementViewCount(articleId, Long.parseLong(increment));

            // 删详情缓存：让下次读回填"新基准"
            stringRedisTemplate.delete(DETAIL_CACHE_PREFIX + articleId);
        }
    }
}

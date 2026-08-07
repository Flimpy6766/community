package com.community.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.entity.Article;
import com.community.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/** 浏览量定时落库：把Redis里的增量批量刷进数据库 */
@Component
@RequiredArgsConstructor
public class ViewCountSyncTask {

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

            // SQL添加增量
            UpdateWrapper<Article> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", articleId)
                    .setSql("view_count = view_count + " + increment);
            articleMapper.update(null, wrapper);

            // 删详情缓存：让下次读回填"新基准"
            stringRedisTemplate.delete(DETAIL_CACHE_PREFIX + articleId);
        }
    }
}

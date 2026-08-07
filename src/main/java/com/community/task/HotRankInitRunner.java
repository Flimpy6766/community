package com.community.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.entity.Article;
import com.community.mapper.ArticleMapper;
import com.community.service.ArticleCounter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/** 热榜冷启动：应用启动时，把数据库的历史热度灌进 ZSet */
@Component
@RequiredArgsConstructor
public class HotRankInitRunner implements CommandLineRunner {

    private final ArticleMapper articleMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) {
        // ZSet 里有数据 -> 跳过，不覆盖实时增量
        Long size = stringRedisTemplate.opsForZSet().zCard(ArticleCounter.HOT_KEY);
        if (size != null && size > 0) {
            return;
        }

        // 查所有已发布文章，算热度，灌进 ZSet
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);
        List<Article> articles = articleMapper.selectList(wrapper);

        for (Article a : articles) {
            double hot = a.getViewCount() + a.getLikeCount() * 2
                    + a.getFavoriteCount() * 3 + a.getCommentCount() * 4;
            stringRedisTemplate.opsForZSet()
                    .add(ArticleCounter.HOT_KEY, String.valueOf(a.getId()), hot);
        }
    }
}

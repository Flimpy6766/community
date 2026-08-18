package com.community.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.article.mapper.ArticleMapper;
import com.community.article.mapper.ArticleTagMapper;
import com.community.article.mapper.CommentMapper;
import com.community.user.mapper.UserMapper;
import com.community.common.exception.BusinessException;
import com.community.admin.dto.request.UpdateUserStatusDTO;
import com.community.article.entity.Article;
import com.community.article.entity.ArticleTag;
import com.community.article.entity.Comment;
import com.community.user.entity.User;
import com.community.common.transaction.AfterCommitExecutor;
import com.community.admin.service.AdminService;
import com.community.article.service.ArticleCounter;
import com.community.article.service.ArticleHotRankService;
import com.community.common.util.SecurityUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {

    public AdminServiceImpl(ArticleMapper articleMapper,
                            UserMapper userMapper,
                            CommentMapper commentMapper,
                            StringRedisTemplate stringRedisTemplate,
                            ArticleTagMapper articleTagMapper,
                            ArticleCounter articleCounter,
                            AfterCommitExecutor afterCommitExecutor,
                            ArticleHotRankService articleHotRankService) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.articleTagMapper = articleTagMapper;
        this.articleCounter = articleCounter;
        this.afterCommitExecutor = afterCommitExecutor;
        this.articleHotRankService = articleHotRankService;
    }

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleCounter articleCounter;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ArticleHotRankService articleHotRankService;


    @Override
    @Transactional
    public void updateUserStatus(Long id, UpdateUserStatusDTO dto) {
        if (dto.getStatus() < 0 || dto.getStatus() > 1) {
            throw new BusinessException("状态只能是0或1");
        }

        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if (id.equals(SecurityUtil.getCurrentUserId())) {
            throw new BusinessException("不能操作自己");
        }

        User update = new User();
        update.setId(id);
        update.setStatus(dto.getStatus());
        userMapper.updateById(update);

        // 禁用 → 数据库事务提交后再删该用户所有 token（踢下线）
        if (dto.getStatus() == 0) {
            afterCommitExecutor.execute(() -> {
                Set<String> keys = stringRedisTemplate.keys("login:token:*");
                if (keys != null) {
                    for (String key : keys) {
                        if (String.valueOf(id).equals(stringRedisTemplate.opsForValue().get(key))) {
                            stringRedisTemplate.delete(key);
                        }
                    }
                }
            });
        }
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException("文章不存在");

        // 与 ArticleServiceImpl.delete 同步：删关系 → 删文章 → 删缓存 → 删热榜
        LambdaQueryWrapper<ArticleTag> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(delWrapper);
        articleMapper.deleteById(id);
        afterCommitExecutor.execute(() -> {
            stringRedisTemplate.delete("article:detail:" + id);
            articleHotRankService.remove(id);
        });
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) throw new BusinessException("评论不存在");

        Article article = articleMapper.selectById(comment.getArticleId());
        if (article == null) throw new BusinessException("文章不存在");

        int deletedCount = 1;
        if (comment.getParentId() == 0) {
            LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Comment::getParentId, id);
            List<Comment> replies = commentMapper.selectList(replyWrapper);
            deletedCount += replies.size();
            commentMapper.delete(replyWrapper);
        }
        commentMapper.deleteById(id);

        // 扣计数（负数增量，热榜同步扣）
        articleCounter.increase(article, ArticleCounter.CountType.COMMENT, -deletedCount);
    }
}

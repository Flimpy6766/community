package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.exception.BusinessException;
import com.community.dto.AdminArticleVO;
import com.community.dto.AdminOverviewVO;
import com.community.dto.UpdateUserStatusDTO;
import com.community.entity.Article;
import com.community.entity.ArticleTag;
import com.community.entity.Comment;
import com.community.entity.User;
import com.community.mapper.*;
import com.community.service.AdminService;
import com.community.service.ArticleCounter;
import com.community.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final TagMapper tagMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleCounter articleCounter;


    @Override
    public AdminOverviewVO overview() {
        AdminOverviewVO vo = new AdminOverviewVO();
        vo.setArticleCount(articleMapper.selectCount(null));   // null = 全表，逻辑删除自动过滤
        vo.setUserCount(userMapper.selectCount(null));
        vo.setCommentCount(commentMapper.selectCount(null));
        vo.setTagCount(tagMapper.selectCount(null));
        return vo;
    }

    @Override
    public IPage<AdminArticleVO> listAll(Integer page, Integer size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Article::getCreateTime);
        Page<Article> result = articleMapper.selectPage(new Page<>(page, size), wrapper);

        if (result == null || result.getRecords().isEmpty()) {
            Page<AdminArticleVO> empty = new Page<>(page, size, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
         }

        // {userId, nickname}
        List<Long> userIds = result.getRecords().stream()
                .map(Article::getUserId).distinct().collect(Collectors.toList());
        Map<Long, String> nicknameMap = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        Page<AdminArticleVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(article -> {
                    AdminArticleVO vo = new AdminArticleVO();
                    BeanUtils.copyProperties(article, vo);
                    vo.setAuthorName(nicknameMap.getOrDefault(article.getUserId(), "已注销"));
                    return vo;
                })
                .collect(Collectors.toList()));

        return voPage;
    }

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

        // 禁用 → 删该用户所有 token（踢下线）
        if (dto.getStatus() == 0) {
            Set<String> keys = stringRedisTemplate.keys("login:token:*");
            if (keys != null) {
                for (String key : keys) {
                    if (String.valueOf(id).equals(stringRedisTemplate.opsForValue().get(key))) {
                        stringRedisTemplate.delete(key);
                    }
                }
            }
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
        stringRedisTemplate.delete("article:detail:" + id);
        stringRedisTemplate.opsForZSet().remove(ArticleCounter.HOT_KEY, String.valueOf(id));
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

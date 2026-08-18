package com.community.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.exception.BusinessException;
import com.community.user.dto.response.response.ArticleVO;
import com.community.user.dto.response.response.CommentVO;
import com.community.article.entity.Article;
import com.community.article.mapper.ArticleMapper;
import com.community.article.mapper.CommentMapper;
import com.community.article.mapper.FavoriteMapper;
import com.community.article.service.ArticleCounter;
import com.community.article.service.ArticleQueryService;
import com.community.article.service.ArticleTagService;
import com.community.common.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ArticleQueryServiceImpl implements ArticleQueryService {

    public ArticleQueryServiceImpl(ArticleMapper articleMapper,
                                   ArticleTagService articleTagService,
                                   FavoriteMapper favoriteMapper,
                                   StringRedisTemplate stringRedisTemplate,
                                   CommentMapper commentMapper) {
        this.articleMapper = articleMapper;
        this.articleTagService = articleTagService;
        this.favoriteMapper = favoriteMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.commentMapper = commentMapper;
    }

    private final ArticleMapper articleMapper;
    private final ArticleTagService articleTagService;
    private final FavoriteMapper favoriteMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CommentMapper commentMapper;

    // 列表文章阅读
    @Override
    public IPage<ArticleVO> list(Integer page, Integer size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
                .orderByDesc(Article::getCreateTime);
        return pageByWrapper(wrapper, page, size);
    }

    // 搜索文章（已发布，标题或正文包含关键字）
    @Override
    public IPage<ArticleVO> search(String keyword, Integer page, Integer size) {

        if (keyword == null || keyword.trim().isEmpty()) {
            Page<ArticleVO> empty = new Page<>(page, size, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
                .and(w -> w.like(Article::getTitle, keyword)
                        .or()
                        .like(Article::getContent, keyword))
                .orderByDesc(Article::getCreateTime);
        return pageByWrapper(wrapper, page, size);
    }

    // 我的文章（status ：null 全部 / 0 草稿 / 1 已发布）
    @Override
    public IPage<ArticleVO> listMyArticles(Integer status, Integer page, Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getUserId, userId)
                .orderByDesc(Article::getCreateTime);
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        return pageByWrapper(wrapper, page, size);
    }

    // 个人收藏列表
    @Override
    public IPage<ArticleVO> listMyFavorites(Integer page, Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        // 通过 Mapper XML 分页查询收藏对应的文章，按收藏时间倒序
        IPage<Article> articlePage = favoriteMapper.selectFavoriteArticles(
                new Page<>(page, size), userId);

        if (articlePage.getRecords().isEmpty()) {
            Page<ArticleVO> empty = new Page<>(page, size, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        List<Article> articles = articlePage.getRecords();

        // 查标签 ids -> tags
        Map<Long, List<String>> tagsMap = articleTagService.findTagNamesByArticleIds(
                articles.stream().map(Article::getId).toList());

        // 文章 -> articleVO -> Page<ArticleVO>
        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(),
                articlePage.getSize(), articlePage.getTotal());
        voPage.setRecords(articles.stream()
                .map(article -> toVO(article, tagsMap))
                .collect(Collectors.toList()));
        return voPage;
    }

    // 评论列表
    @Override
    public IPage<CommentVO> listComments(Long articleId, Integer page, Integer size) {
        // Mapper XML 直接关联用户查询顶级评论，按时间倒序分页
        IPage<CommentVO> topPage = commentMapper.selectTopCommentVOs(
                new Page<>(page, size), articleId);

        if (topPage.getRecords().isEmpty()) {
            Page<CommentVO> empty = new Page<>(page, size, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        // 查询所有回复 topIds -> replies
        List<Long> topIds = topPage.getRecords().stream()
                .map(CommentVO::getId)
                .collect(Collectors.toList());
        List<CommentVO> replies = commentMapper.selectReplyVOs(articleId, topIds);

        // 回复按 parentId 分组
        Map<Long, List<CommentVO>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        List<CommentVO> voList = topPage.getRecords().stream()
                .map(vo -> {
                    vo.setReplies(replyMap.getOrDefault(vo.getId(), Collections.emptyList()));
                    return vo;
                })
                .collect(Collectors.toList());
        Page<CommentVO> voPage = new Page<>(topPage.getCurrent(), topPage.getSize(), topPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    // 热榜
    @Override
    public List<ArticleVO> hotList(Integer limit) {
        // 按分数倒序取前 N 名→ 拿到文章 id
        Set<String> ids = stringRedisTemplate.opsForZSet()
                .reverseRange(ArticleCounter.HOT_KEY, 0, limit - 1);

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // id -> {id, article}, {id, tags}
        List<Long> articleIds = ids.stream().map(Long::valueOf).collect(Collectors.toList());
        Map<Long, Article> articleMap = articleMapper.selectByIds(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, a -> a));
        Map<Long, List<String>> tagsMap = articleTagService.findTagNamesByArticleIds(articleIds);

        // 按 ZSet 顺序组装
        return articleIds.stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .map(article -> toVO(article, tagsMap))
                .collect(Collectors.toList());
    }

    /** 按条件查文章分页 + 组装标签 : article table */
    private IPage<ArticleVO> pageByWrapper(LambdaQueryWrapper<Article> wrapper,
                                           Integer page, Integer size) {
        Page<Article> pageObj = new Page<>(page, size);
        Page<Article> result = articleMapper.selectPage(pageObj, wrapper);

        if (result == null || result.getRecords().isEmpty()) {
            Page<ArticleVO> empty = new Page<>(page, size, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }


        // 批量查询
        Map<Long, List<String>> tagsMap = articleTagService.findTagNamesByArticleIds(result.getRecords()
                .stream().map(Article::getId).toList());

        // 每一篇从 tagsMap 获取对应标签
        Page<ArticleVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(article -> toVO(article, tagsMap))
                .collect(Collectors.toList()));

        return voPage;
    }

    /** 标签从现成的 Map 取, 再 Article → ArticleVO*/
    private ArticleVO toVO(Article article, Map<Long, List<String>> tagsMap) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        vo.setTags(tagsMap.getOrDefault(article.getId(), Collections.emptyList()));
        return vo;
    }
}

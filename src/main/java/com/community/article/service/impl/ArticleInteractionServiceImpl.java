package com.community.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.article.dto.request.CreateCommentDTO;
import com.community.user.dto.response.response.FavoriteVO;
import com.community.user.dto.response.response.LikeVO;
import com.community.article.entity.Article;
import com.community.article.entity.Comment;
import com.community.article.entity.Favorite;
import com.community.article.entity.UserLike;
import com.community.article.mapper.ArticleMapper;
import com.community.article.mapper.CommentMapper;
import com.community.article.mapper.FavoriteMapper;
import com.community.user.mapper.UserLikeMapper;
import com.community.article.service.ArticleCounter;
import com.community.article.service.ArticleInteractionService;
import com.community.common.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleInteractionServiceImpl implements ArticleInteractionService {

    private final ArticleMapper articleMapper;
    private final UserLikeMapper userLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final ArticleCounter articleCounter;

    public ArticleInteractionServiceImpl(
            ArticleMapper articleMapper,
            UserLikeMapper userLikeMapper,
            FavoriteMapper favoriteMapper,
            CommentMapper commentMapper,
            ArticleCounter articleCounter) {
        this.articleMapper = articleMapper;
        this.userLikeMapper = userLikeMapper;
        this.favoriteMapper = favoriteMapper;
        this.commentMapper = commentMapper;
        this.articleCounter = articleCounter;
    }

    @Override
    @Transactional
    public LikeVO toggleLike(Long articleId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new BusinessException("文章不存在");

        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getUserId, userId)
                .eq(UserLike::getArticleId, articleId);
        UserLike userLike = userLikeMapper.selectOne(wrapper);

        boolean liked;
        if (userLike == null) {
            UserLike like = new UserLike();
            like.setUserId(userId);
            like.setArticleId(articleId);
            userLikeMapper.insert(like);
            articleCounter.increase(article, ArticleCounter.CountType.LIKE, 1);
            liked = true;
        } else {
            userLikeMapper.deleteById(userLike.getId());
            articleCounter.increase(article, ArticleCounter.CountType.LIKE, -1);
            liked = false;
        }

        LikeVO vo = new LikeVO();
        vo.setLiked(liked);
        vo.setLikeCount(article.getLikeCount());
        return vo;
    }

    @Override
    @Transactional
    public FavoriteVO toggleFavorite(Long articleId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new BusinessException("文章不存在");

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId);
        Favorite favorite = favoriteMapper.selectOne(wrapper);

        boolean favorited;
        if (favorite == null) {
            Favorite newFavorite = new Favorite();
            newFavorite.setUserId(userId);
            newFavorite.setArticleId(articleId);
            favoriteMapper.insert(newFavorite);
            articleCounter.increase(article, ArticleCounter.CountType.FAVORITE, 1);
            favorited = true;
        } else {
            favoriteMapper.deleteById(favorite.getId());
            articleCounter.increase(article, ArticleCounter.CountType.FAVORITE, -1);
            favorited = false;
        }

        FavoriteVO vo = new FavoriteVO();
        vo.setFavorited(favorited);
        vo.setFavoriteCount(article.getFavoriteCount());
        return vo;
    }

    @Override
    @Transactional
    public void addComment(Long articleId, CreateCommentDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new BusinessException("文章不存在");

        if (dto.getParentId() > 0) {
            Comment parent = commentMapper.selectById(dto.getParentId());
            if (parent == null || !parent.getArticleId().equals(articleId)) {
                throw new BusinessException("回复的评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setContent(dto.getContent());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        commentMapper.insert(comment);

        articleCounter.increase(article, ArticleCounter.CountType.COMMENT, 1);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        Comment comment = commentMapper.selectById(id);
        if (comment == null) throw new BusinessException("评论不存在");
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该评论");
        }

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

        articleCounter.increase(article, ArticleCounter.CountType.COMMENT, -deletedCount);
    }
}

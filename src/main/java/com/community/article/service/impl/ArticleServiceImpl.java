package com.community.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.transaction.AfterCommitExecutor;
import com.community.article.dto.request.CreateArticleDTO;
import com.community.user.dto.response.response.ArticleVO;
import com.community.article.entity.Article;
import com.community.article.entity.Favorite;
import com.community.article.entity.UserLike;
import com.community.article.mapper.ArticleMapper;
import com.community.article.mapper.FavoriteMapper;
import com.community.user.mapper.UserLikeMapper;
import com.community.article.service.ArticleCounter;
import com.community.article.service.ArticleHotRankService;
import com.community.article.service.ArticleService;
import com.community.article.service.ArticleTagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import static com.community.common.util.SecurityUtil.getCurrentUserId;

@Service
public class ArticleServiceImpl implements ArticleService {

    public ArticleServiceImpl(ArticleTagService articleTagService,
                              StringRedisTemplate stringRedisTemplate,
                              ArticleMapper articleMapper,
                              UserLikeMapper userLikeMapper,
                              FavoriteMapper favoriteMapper,
                              ArticleCounter articleCounter,
                              AfterCommitExecutor afterCommitExecutor,
                              ArticleHotRankService articleHotRankService,
                              ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.articleMapper = articleMapper;
        this.userLikeMapper = userLikeMapper;
        this.favoriteMapper = favoriteMapper;
        this.articleCounter = articleCounter;
        this.afterCommitExecutor = afterCommitExecutor;
        this.articleHotRankService = articleHotRankService;
        this.articleTagService = articleTagService;
        this.objectMapper = objectMapper;
    }

    private final StringRedisTemplate stringRedisTemplate;//Jackson, Spring boot 已经配置好的
    private final ArticleMapper articleMapper;
    private final UserLikeMapper userLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final ArticleCounter articleCounter;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ArticleHotRankService articleHotRankService;
    private final ArticleTagService articleTagService;
    private final ObjectMapper objectMapper;


    // 创建文章草稿 (status == 0)
    @Override
    @Transactional
    public Long create(CreateArticleDTO dto) {
        // 作者从 SecurityContext 取（过滤器放进去的身份）
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        // 先插入文章
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCover(dto.getCover());
        // status 没设 → MyBatis-Plus 忽略 null → 数据库默认 0（草稿）
        articleMapper.insert(article);

        articleTagService.replaceTags(article.getId(), dto.getTags());

        return article.getId();
    }

    // 更新文章
    @Override
    @Transactional
    public void update(Long id, CreateArticleDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 作者校验
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(article.getUserId())) {
            throw new BusinessException("文章不存在");
        }

        // 更新字段
        Article update = new Article();
        update.setTitle(dto.getTitle());
        update.setSummary(dto.getSummary());
        update.setContent(dto.getContent());
        update.setCover(dto.getCover());
        if (dto.getStatus() != null) {
            update.setStatus(dto.getStatus());
        }
        update.setId(id);
        articleMapper.updateById(update);

        // 标签：先删旧关系，再插新关系
        articleTagService.deleteByArticleId(id);

        articleTagService.replaceTags(article.getId(), dto.getTags());

        // 数据库事务提交后再删除详情缓存
        afterCommitExecutor.execute(() ->
                stringRedisTemplate.delete(ARTICLE_CACHE_KEY + id));
    }

    // 删除文章
    @Override
    @Transactional
    public void delete(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(article.getUserId())) {
            throw new BusinessException("文章不存在");
        }

        // 先删关系，再删文章
        articleTagService.deleteByArticleId(id);
        articleMapper.deleteById(id);

        // 数据库事务提交后再删除缓存和热榜成员
        afterCommitExecutor.execute(() -> {
            stringRedisTemplate.delete(ARTICLE_CACHE_KEY + id);
            articleHotRankService.remove(id);
        });
    }

    private static final String ARTICLE_CACHE_KEY = "article:detail:";

    // 单篇文章阅读 + Redis 热点文章缓存
    @Override
    public ArticleVO getDetail(Long id) {
        // 先查 Redis 缓存（命中返回，但计数以数据库为准，避免点赞/收藏/评论后数字过期）
        ArticleVO vo = null;
        String cached = stringRedisTemplate.opsForValue().get(ARTICLE_CACHE_KEY + id);
        if (cached != null) {
            try {
                vo = objectMapper.readValue(cached, ArticleVO.class);
            } catch (JsonProcessingException e) {
                // 反序列化失败（格式坏了）：删掉缓存，走正常查库
                stringRedisTemplate.delete(ARTICLE_CACHE_KEY + id);
            }
        }

        // 再查数据库：拿最新计数 + 校验文章是否存在
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 未发布：仅作者可见（不计数、不缓存）
        if (article.getStatus() == 0) {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(article.getUserId())) {
                throw new BusinessException("文章不存在");
            }
            return toVO(article);
        }

        // 已发布：浏览量计数（Redis 增量，定时落库）
        articleCounter.increaseView(id);

        if (vo == null) {
            // 缓存未命中：组装并写入缓存
            vo = toVO(article);
            try {
                stringRedisTemplate.opsForValue().set(
                        ARTICLE_CACHE_KEY + id,
                        objectMapper.writeValueAsString(vo),
                        Duration.ofMinutes(30));
            } catch (JsonProcessingException ignored) {
            }
        } else {
            // 缓存命中：用数据库最新计数覆盖，防止缓存期间点赞/收藏/评论导致数字过期
            vo.setLikeCount(article.getLikeCount());
            vo.setFavoriteCount(article.getFavoriteCount());
            vo.setCommentCount(article.getCommentCount());
        }
        vo.setViewCount(article.getViewCount() + (int) articleCounter.getViewIncrement(id));

        // 登录用户：带出"我是否已点赞 / 已收藏"，前端按钮才能正确显示
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            vo.setLiked(userLikeMapper.selectOne(new LambdaQueryWrapper<UserLike>()
                    .eq(UserLike::getUserId, currentUserId)
                    .eq(UserLike::getArticleId, id)) != null);
            vo.setFavorited(favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, currentUserId)
                    .eq(Favorite::getArticleId, id)) != null);
        }

        return vo;
    }

    /** Article → ArticleVO，并查这篇的标签名 */
    private ArticleVO toVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);   // 同名字段自动复制
        vo.setTags(articleTagService.findTagNames(article.getId()));
        return vo;
    }
}

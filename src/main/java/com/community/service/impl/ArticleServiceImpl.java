package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.exception.BusinessException;
import com.community.dto.*;
import com.community.entity.*;
import com.community.mapper.*;
import com.community.service.ArticleCounter;
import com.community.service.ArticleService;
import com.community.util.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.community.util.SecurityUtil.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper; //Jackson, Spring boot 已经配置好的
    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final UserLikeMapper userLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final ArticleCounter articleCounter;


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


        if(dto.getTags() != null) {
            for (String tagName : dto.getTags()) {
                // 查询数据库有没有这个标签，没有就新建
                LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Tag::getName, tagName);
                Tag tag = tagMapper.selectOne(wrapper);

                if (tag == null) {
                    tag = new Tag();
                    tag.setName(tagName);
                    tagMapper.insert(tag);
                }

                // 与当前文章建立关系
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tag.getId());
                articleTagMapper.insert(articleTag);
            }
        }

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
        LambdaQueryWrapper<ArticleTag> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(delWrapper);

        if (dto.getTags() != null) {
            for (String tagName : dto.getTags()) {
                LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Tag::getName, tagName);
                Tag tag = tagMapper.selectOne(wrapper);
                if (tag == null) {
                    tag = new Tag();
                    tag.setName(tagName);
                    tagMapper.insert(tag);
                }
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(id);
                articleTag.setTagId(tag.getId());
                articleTagMapper.insert(articleTag);
            }
        }

        // 删除 Redis 缓存
        stringRedisTemplate.delete(ARTICLE_CACHE_KEY + id);
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
        LambdaQueryWrapper<ArticleTag> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(delWrapper);
        articleMapper.deleteById(id);

        // 删除 Redis 缓存
        stringRedisTemplate.delete(ARTICLE_CACHE_KEY + id);
        // 删热榜
        stringRedisTemplate.opsForZSet().remove(ArticleCounter.HOT_KEY, String.valueOf(id));
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

    // 点赞
    @Override
    @Transactional
    public LikeVO toggleLike(Long articleId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        // 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new BusinessException("文章不存在");

        // 检查用户是否点赞过
        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getUserId, userId)
                .eq(UserLike::getArticleId, articleId);
        UserLike userLike = userLikeMapper.selectOne(wrapper);

        boolean liked;
        if (userLike == null) {
            // 没赞过 → 点赞：插记录 + 计数 +1
            UserLike like = new UserLike();
            like.setUserId(userId);
            like.setArticleId(articleId);
            userLikeMapper.insert(like);
            articleCounter.increase(article, ArticleCounter.CountType.LIKE, 1);
            liked = true;
        } else {
            // 赞过 → 取消：删记录 + 计数 -1
            userLikeMapper.deleteById(userLike.getId());
            articleCounter.increase(article, ArticleCounter.CountType.LIKE, -1);
            liked = false;
        }

        LikeVO vo = new LikeVO();
        vo.setLiked(liked);
        vo.setLikeCount(article.getLikeCount());
        return vo;
    }

    // 收藏
    @Override
    @Transactional
    public FavoriteVO toggleFavorite(Long articleId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        // 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new BusinessException("文章不存在");

        // 检查用户是否收藏过
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId);
        Favorite favorite = favoriteMapper.selectOne(wrapper);

        boolean favorited;
        if (favorite == null) {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setArticleId(articleId);
            favoriteMapper.insert(f);
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
    @Transactional
    public IPage<ArticleVO> listMyFavorites(Integer page, Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        // 分页查找个人收藏记录, 时间顺序 {f1, f2, f3} userId == xxx
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);
        Page<Favorite> favoritePage = favoriteMapper.selectPage(new Page<>(page, size), wrapper);

        if (favoritePage.getRecords().isEmpty()) {
            Page<ArticleVO> empty = new Page<>(page, size, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        // 查文章 favoritePage -> articleId -> article
        List<Long> articleIds = favoritePage.getRecords().stream()
                .map(Favorite::getArticleId)
                .collect(Collectors.toList());
        List<Article> articles = articleMapper.selectByIds(articleIds);

        // Map 索引 articleId : article
        Map<Long, Article> articleMap = articles.stream()
                .collect(Collectors.toMap(Article::getId, a -> a));

        // 查标签 articles -> tags
        Map<Long, List<String>> tagsMap = batchFindTags(articles);

        // favoritePage -> article (过滤已删除文章) -> articleVO -> Page<ArticleVO>
        Page<ArticleVO> voPage = new Page<>(page, size, favoritePage.getTotal());
        voPage.setRecords(favoritePage.getRecords().stream()
                .map(favorite -> articleMap.get(favorite.getArticleId()))
                .filter(Objects::nonNull)
                .map(article -> toVO(article, tagsMap))
                .collect(Collectors.toList()));
        return voPage;
    }

    // 添加评论
    @Override
    @Transactional
    public void addComment(Long articleId, CreateCommentDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new BusinessException("文章不存在");

        if (dto.getParentId() > 0) {
            Comment parent = commentMapper.selectById(dto.getParentId());
            if (parent == null || !parent.getArticleId().equals(articleId))
                throw new BusinessException("回复的评论不存在");
        }

        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setContent(dto.getContent());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        commentMapper.insert(comment);

        articleCounter.increase(article, ArticleCounter.CountType.COMMENT, 1);
    }

    // 删除评论
    @Override
    @Transactional
    public void deleteComment(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        // 只能删自己的
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该评论");
        }

        Article article = articleMapper.selectById(comment.getArticleId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        int deletedCount = 1;
        if (comment.getParentId() == 0) {
            // 顶级评论：级联删它的所有回复
            LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Comment::getParentId, id);
            List<Comment> replies = commentMapper.selectList(replyWrapper);
            deletedCount += replies.size();
            commentMapper.delete(replyWrapper);
        }
        commentMapper.deleteById(id);

        // 计数 : 顶级 = 1 + 回复数，回复 = 1
        articleCounter.increase(article, ArticleCounter.CountType.COMMENT, deletedCount);
    }

    // 评论列表
    @Override
    public IPage<CommentVO> listComments(Long articleId, Integer page, Integer size) {
        // 顶级评论分页 (parentId == 0) 时间顺序
        LambdaQueryWrapper<Comment> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getParentId, 0)
                .orderByDesc(Comment::getCreateTime);
        Page<Comment> topPage = commentMapper.selectPage(new Page<>(page, size), topWrapper);

        if (topPage.getRecords().isEmpty()) {
            Page<CommentVO> empty = new Page<>(page, size, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        // 查询所有回复 topIds -> replies
        List<Long> topIds = topPage.getRecords().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.in(Comment::getParentId, topIds)
                .eq(Comment::getArticleId, articleId)
                .orderByDesc(Comment::getCreateTime);
        List<Comment> replies = commentMapper.selectList(replyWrapper);

        // 收集用户 Id (顶层 + 回复) {id, nickname}
        Set<Long> userIds = new HashSet<>();
        topPage.getRecords().forEach(c -> userIds.add(c.getUserId()));
        replies.forEach(c -> userIds.add(c.getUserId()));
        Map<Long, String> nicknameMap = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        // 回复分组 {parentId, replies} replies: List<Comment> -> List<CommentVO>
        Map<Long, List<CommentVO>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(
                        Comment::getParentId,
                        Collectors.mapping(c -> toCommentVO(c, nicknameMap), Collectors.toList())));

        List<CommentVO> voList = topPage.getRecords().stream()
                .map(c -> {
                    CommentVO vo = toCommentVO(c, nicknameMap);
                    vo.setReplies(replyMap.getOrDefault(c.getId(), Collections.emptyList()));
                    return vo;
                })
                .collect(Collectors.toList());
        Page<CommentVO> voPage = new Page<>(page, size, topPage.getTotal());
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
        Map<Long, List<String>> tagsMap = batchFindTags(new ArrayList<>(articleMap.values()));

        // 按 ZSet 顺序组装
        return articleIds.stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .map(article -> toVO(article, tagsMap))
                .collect(Collectors.toList());
    }

    /** Article → ArticleVO，并查这篇的标签名 */
    private ArticleVO toVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);   // 同名字段自动复制
        vo.setTags(findTagNames(article.getId()));
        return vo;
    }

    /** 标签从现成的 Map 取, 再 Article → ArticleVO*/
    private ArticleVO toVO(Article article, Map<Long, List<String>> tagsMap) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        vo.setTags(tagsMap.getOrDefault(article.getId(), Collections.emptyList()));
        return vo;
    }

    /** 评论 → VO 补上昵称 */
    private CommentVO toCommentVO(Comment comment, Map<Long, String> nicknameMap) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setNickname(nicknameMap.getOrDefault(comment.getUserId(), "用户"));  // 用户可能已注销
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    /** 查某篇文章的所有标签名（关系表反查） */
    private List<String> findTagNames(Long articleId) {
        // 关系表：找这篇文章的所有关系行
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> relations = articleTagMapper.selectList(wrapper);

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        // 取出 tag_id 列表 → 批量查标签 → 取名字
        List<Long> tagIds = relations.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());
        return tagMapper.selectByIds(tagIds).stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    /** 批量查标签：articleId → [标签名...]*/
    private Map<Long, List<String>> batchFindTags(List<Article> articles) {
        if (articles.isEmpty()) {
            return Collections.emptyMap();
        }

        // 收集所有文章 id
        List<Long> articleIds = articles.stream()
                .map(Article::getId).collect(Collectors.toList());

        // 一次查清楚所有关系 (IN 查询)
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ArticleTag::getArticleId, articleIds);
        List<ArticleTag> relations = articleTagMapper.selectList(wrapper);

        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }

        // 按照文章分标签 articleId → [tagId, tagId...]
        Map<Long, List<Long>> tagIdsByArticle = relations.stream()
                .collect(Collectors.groupingBy(
                        ArticleTag::getArticleId,
                        Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));

        // 标签去重, 并且查询 tagId -> tagName
        List<Long> allTagIds = relations.stream()
                .map(ArticleTag::getTagId)
                .distinct().collect(Collectors.toList());
        Map<Long, String> tagNameMap = tagMapper.selectByIds(allTagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));

        // 将文章打上对应标签 articleId -> tagNames
        Map<Long, List<String>> result = new HashMap<>();
        tagIdsByArticle.forEach((articleId, tagIds) -> result
                .put(articleId, tagIds.stream().map(tagNameMap::get).collect(Collectors.toList())));

        return result;
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
        Map<Long, List<String>> tagsMap = batchFindTags(result.getRecords());

        // 每一篇从 tagsMap 获取对应标签
        Page<ArticleVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(article -> toVO(article, tagsMap))
                .collect(Collectors.toList()));

        return voPage;
    }



}

package com.community.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.common.result.Result;
import com.community.dto.*;
import com.community.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章接口：文章 CRUD、互动（点赞/收藏/评论）、热榜
 * <p>公开接口：GET /article/**（浏览）</p>
 * <p>登录接口：POST/PUT/DELETE（写操作）</p>
 *
 * @author community
 */
@RestController
@RequestMapping("/article")
@Validated
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 发布文章
     * <p>作者取当前登录用户；tags 传标签名列表，不存在的标签自动创建；status 不传默认草稿(0)</p>
     *
     * @param dto 文章内容（标题、摘要、正文、封面、状态、标签）
     * @return 新文章 id
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CreateArticleDTO dto) {
        return Result.success(articleService.create(dto));
    }

    /**
     * 更新文章（仅作者）
     * <p>status 传 1 表示发布，传 0 表示存草稿；标签全删全插</p>
     *
     * @param id  文章 id
     * @param dto 新的文章内容
     * @return 统一返回体，无数据
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CreateArticleDTO dto) {
        articleService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除文章（仅作者）
     * <p>级联删除文章的标签关系，并从热榜移除</p>
     *
     * @param id 文章 id
     * @return 统一返回体，无数据
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    /**
     * 文章列表（公开）
     * <p>只返回已发布文章，按创建时间倒序，带标签</p>
     *
     * @param page 页码（从 1 开始，默认 1）
     * @param size 每页条数（默认 10）
     * @return 分页结果：records/total/size/current/pages
     */
    @GetMapping("/list")
    public Result<IPage<ArticleVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(articleService.list(page, size));
    }

    /**
     * 文章详情（公开）
     * <p>已发布文章所有人可见，浏览量 Redis 计数；草稿仅作者可见（不计数）</p>
     *
     * @param id 文章 id
     * @return 文章详情（含标签、实时浏览量）
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.getDetail(id));
    }

    /**
     * 点赞/取消点赞（登录，toggle 切换）
     *
     * @param id 文章 id
     * @return 最新状态：liked（是否已赞）、likeCount（最新点赞数）
     */
    @PostMapping("/{id}/like")
    public Result<LikeVO> like(@PathVariable Long id) {
        return Result.success(articleService.toggleLike(id));
    }

    /**
     * 收藏/取消收藏（登录，toggle 切换）
     *
     * @param id 文章 id
     * @return 最新状态：favorited（是否已收藏）、favoriteCount（最新收藏数）
     */
    @PostMapping("/{id}/favorite")
    public Result<FavoriteVO> favorite(@PathVariable Long id) {
        return Result.success(articleService.toggleFavorite(id));
    }

    /**
     * 我的收藏列表（登录）
     * <p>按收藏时间倒序，带标签</p>
     *
     * @param page 页码（默认 1）
     * @param size 每页条数（默认 10）
     * @return 分页结果：我收藏的文章
     */
    @GetMapping("/favorite/my")
    public Result<IPage<ArticleVO>> myFavorites(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(articleService.listMyFavorites(page, size));
    }

    /**
     * 发表评论/回复（登录）
     * <p>parentId 不传或 0 为顶级评论，传评论 id 则为回复该评论</p>
     *
     * @param id  文章 id
     * @param dto 评论内容（content、parentId）
     * @return 统一返回体，无数据
     */
    @PostMapping("/{id}/comment")
    public Result<Void> comment(@PathVariable Long id, @Valid @RequestBody CreateCommentDTO dto) {
        articleService.addComment(id, dto);
        return Result.success();
    }

    /**
     * 删除评论（仅评论作者）
     * <p>删除顶级评论时级联删除其下所有回复</p>
     *
     * @param id 评论 id
     * @return 统一返回体，无数据
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        articleService.deleteComment(id);
        return Result.success();
    }

    /**
     * 评论列表（公开）
     * <p>顶级评论分页（时间正序），回复全量带出；每条评论附带作者昵称</p>
     *
     * @param id   文章 id
     * @param page 页码（默认 1）
     * @param size 每页条数（默认 20）
     * @return 分页结果：顶级评论 + 各自 replies
     */
    @GetMapping("/{id}/comments")
    public Result<IPage<CommentVO>> comments(@PathVariable Long id,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(articleService.listComments(id, page, size));
    }

    /**
     * 热榜（公开）
     * <p>按热度倒序：热度 = 浏览×1 + 点赞×2 + 收藏×3 + 评论×4，来源 Redis ZSet</p>
     *
     * @param limit 返回条数（默认 10）
     * @return 热度最高的文章列表（带标签）
     */
    @GetMapping("/hot")
    public Result<List<ArticleVO>> hot(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(articleService.hotList(limit));
    }
}

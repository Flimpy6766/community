package com.community.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.common.result.Result;
import com.community.admin.dto.response.AdminArticleVO;
import com.community.admin.dto.response.AdminOverviewVO;
import com.community.admin.dto.request.UpdateUserStatusDTO;
import com.community.admin.service.AdminQueryService;
import com.community.admin.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    public AdminController(AdminService adminService,
                      AdminQueryService adminQueryService) {
        this.adminService = adminService;
        this.adminQueryService = adminQueryService;
    }

    private final AdminService adminService;
    private final AdminQueryService adminQueryService;

    /**
     * 统计面板（仅管理员）
     * <p>返回文章数、用户数、评论数、标签数，已删除内容自动排除</p>
     *
     * @return 各项总数
     */
    @GetMapping("/overview")
    public Result<AdminOverviewVO> overview() {
        return Result.success(adminQueryService.overview());
    }

    /**
     * 所有文章列表（仅管理员）
     * <p>草稿和已发布都在内，按创建时间倒序，附带作者昵称</p>
     *
     * @param page 页码（默认 1）
     * @param size 每页条数（默认 10）
     * @return 分页结果：后台文章列表
     */
    @GetMapping("/articles")
    public Result<IPage<AdminArticleVO>> articles(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminQueryService.listAll(page, size));
    }

    /**
     * 禁用/启用用户（仅管理员）
     * <p>禁用后该用户所有登录态立即失效；不能操作自己</p>
     *
     * @param id  用户 id
     * @param dto 新状态（0 禁用 / 1 启用）
     * @return 统一返回体，无数据
     */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                         @Valid @RequestBody UpdateUserStatusDTO dto) {
        adminService.updateUserStatus(id, dto);
        return Result.success();
    }

    /**
     * 删除任意文章（仅管理员）
     * <p>级联删除标签关系，清除缓存和热榜</p>
     *
     * @param id 文章 id
     * @return 统一返回体，无数据
     */
    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        adminService.deleteArticle(id);
        return Result.success();
    }

    /**
     * 删除任意评论（仅管理员）
     * <p>删除顶级评论时级联删除其回复，同步扣减评论计数</p>
     *
     * @param id 评论 id
     * @return 统一返回体，无数据
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return Result.success();
    }

}

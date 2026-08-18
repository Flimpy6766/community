package com.community.user;

import com.community.common.result.Result;
import com.community.user.dto.request.LoginDTO;
import com.community.user.dto.request.RegisterDTO;
import com.community.user.dto.request.UpdateProfileDTO;
import com.community.user.dto.response.LoginVO;
import com.community.user.dto.response.UserProfileVO;
import com.community.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口：注册、登录、当前用户信息
 *
 * @author community
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private final UserService userService;

    /**
     * 用户注册
     * <p>用户名唯一，密码使用 BCrypt 加密存储</p>
     *
     * @param dto 注册信息（用户名、密码、昵称）
     * @return 统一返回体，注册成功无数据
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    /**
     * 用户登录
     * <p>成功后返回 JWT token，token 已存入 Redis（有效期 7 天）</p>
     *
     * @param dto 登录信息（用户名、密码）
     * @return token 和用户基本信息
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 获取当前登录用户 id
     * <p>身份来自请求头 Authorization 中的 JWT token，由过滤器解析</p>
     *
     * @return 当前登录用户的 userId
     */
    @GetMapping("/info")
    public Result<Long> info() {
        // 从 SecurityContext 取出过滤器放进去的身份（userId）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userId);
    }

    /**
     * 个人中心资料（登录）
     * <p>基本信息 + 统计：我的文章数、收藏数、评论数、收到的赞数</p>
     *
     * @return 个人资料和统计
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.success(userService.profile());
    }

    /**
     * 修改个人资料（登录）
     * <p>部分更新：传什么改什么，没传的字段不变（昵称/头像/简介）</p>
     *
     * @param dto 要修改的字段
     * @return 统一返回体，无数据
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        userService.updateProfile(dto);
        return Result.success();
    }
}

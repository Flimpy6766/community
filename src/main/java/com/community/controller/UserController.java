package com.community.controller;

import com.community.common.result.Result;
import com.community.dto.LoginDTO;
import com.community.dto.LoginVO;
import com.community.dto.RegisterDTO;
import com.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口：注册、登录、当前用户信息
 *
 * @author community
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

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
}

package com.community.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.user.dto.request.LoginDTO;
import com.community.user.dto.request.RegisterDTO;
import com.community.user.dto.request.UpdateProfileDTO;
import com.community.user.dto.response.LoginVO;
import com.community.user.dto.response.UserProfileVO;
import com.community.article.mapper.row.UserProfileStats;
import com.community.user.entity.User;
import com.community.user.mapper.UserMapper;
import com.community.user.service.UserService;
import com.community.common.util.JwtUtil;
import com.community.common.util.SecurityUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;


@Service
public class UserServiceImpl implements UserService {

    public UserServiceImpl(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void register(RegisterDTO dto) {
        // 查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        Long count = userMapper.selectCount(wrapper);

        if(count > 0) throw new BusinessException(ResultCode.FAILED.getCode(), "用户名已存在");

        // BCrypt 加密密码（绝不存明文）
        String encoded = passwordEncoder.encode(dto.getPassword());

        // 组装实体，插入数据库
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoded);
        user.setStatus(1);
        user.setNickname(dto.getNickname());
        userMapper.insert(user);
    }

    @Override
    @Transactional
    public LoginVO login(LoginDTO dto) {
        // 查用户(密码对照，需查库)
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        // 统一回复
        if(user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new BusinessException("用户名或者密码错误");

        // 被禁用用户无法登录
        if (user.getStatus() == 0) throw new BusinessException("账号已被禁用");

        // 生成Jwt token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 存Redis：key = token , value = userId, 7天过期(和JWT一致)
        stringRedisTemplate.opsForValue().set(
                "login:token:" + token,
                String.valueOf(user.getId()),
                Duration.ofDays(7)
        );

        // 组装返回（不含密码）
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    // 个人中心资料
    @Override
    public UserProfileVO profile() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setCreateTime(user.getCreateTime());

        UserProfileStats stats = userMapper.selectProfileStats(userId);
        vo.setArticleCount(stats.getArticleCount());
        vo.setFavoriteCount(stats.getFavoriteCount());
        vo.setCommentCount(stats.getCommentCount());
        vo.setLikeReceivedCount(stats.getLikeReceivedCount());
        return vo;
    }

    // 个人资料更新
    @Override
    @Transactional
    public void updateProfile(UpdateProfileDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException("请先登录");

        User update = new User();
        update.setId(userId);
        update.setNickname(dto.getNickname());
        update.setAvatar(dto.getAvatar());
        update.setBio(dto.getBio());
        userMapper.updateById(update);   // null 字段不更新
    }
}

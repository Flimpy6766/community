package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.dto.*;
import com.community.entity.*;
import com.community.mapper.*;
import com.community.service.UserService;
import com.community.util.JwtUtil;
import com.community.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final ArticleMapper articleMapper;
    private final FavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final UserLikeMapper userLikeMapper;

    @Override
    @Transactional
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

        // 统计1-3：直接 count
        vo.setArticleCount(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getUserId, userId)));
        vo.setFavoriteCount(favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId)));
        vo.setCommentCount(commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, userId)));

        // 统计4：两步 —— 先我的文章id，再数被赞
        List<Long> myArticleIds = articleMapper.selectList(
                        new LambdaQueryWrapper<Article>().eq(Article::getUserId, userId))
                .stream().map(Article::getId).collect(Collectors.toList());
        vo.setLikeReceivedCount(myArticleIds.isEmpty() ? 0L : userLikeMapper.selectCount(
                new LambdaQueryWrapper<UserLike>().in(UserLike::getArticleId, myArticleIds)));
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

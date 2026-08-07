package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.dto.LoginDTO;
import com.community.dto.LoginVO;
import com.community.dto.RegisterDTO;
import com.community.entity.User;
import com.community.mapper.UserMapper;
import com.community.service.UserService;
import com.community.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final StringRedisTemplate stringRedisTemplate;

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

        // 生成Jwt token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

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
}

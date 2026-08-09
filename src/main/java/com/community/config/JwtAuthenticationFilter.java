package com.community.config;


import com.community.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    protected void doFilterInternal
            (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 取请求头(前端约定放在 Authorization 里，格式 "Bearer xxx")
        String authHeader = request.getHeader("Authorization");

        // 没有 token 或格式不对 → 不处理，放行（Security 的规则决定拦不拦）
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 去掉前缀，拿到 token(Bearer ) 7字符
        String token = authHeader.substring(7);

        // 检查一下，当前请求是不是已经认证过了？如果已经认证了，就不用重复认证了
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try{

                // 用 JwtUtil 解析这个 token，把里面的数据拆出来
                Claims claims = jwtUtil.parseToken(token);

                // 去 Redis 里查 key 是 'login:token:' + token 的值，看存不存在
                String redisUserId = stringRedisTemplate.opsForValue().get("login:token:" + token);

                // 如果 Redis 里查到了值，说明这个 token 是有效的（用户没退出登录，也没被踢下线），那就让认证通过
                if (redisUserId != null) {

                    // 造一张'临时身份证'，表示用户已登录
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            Long.valueOf(claims.get("userId").toString()), // 身份：userId
                            null,                                          // 凭证：存储密码等等信息
                            List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role")))                        // 权限: 存储角色权限等等
                    );

                    // 记录下这个请求的来源信息（IP、SessionId 等），方便以后查看
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 把这张'临时身份证'放进 SecurityContext，表示当前请求的用户已认证通过
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {e.printStackTrace();}

            filterChain.doFilter(request, response);
        }
    }
}

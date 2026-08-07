package com.community.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 关掉默认登录页和弹窗认证
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 定义"哪扇门开、哪扇门锁"
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/article/**").permitAll()
                        .requestMatchers("/user/register", "/user/login", "/test/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(
                        ex -> ex.authenticationEntryPoint((
                                request,
                                response,
                                authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // 401
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
                }))
                // 在用户密码验证前验证
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
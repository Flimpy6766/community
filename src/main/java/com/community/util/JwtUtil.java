package com.community.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtUtil {


    /** 从配置读取的签名密钥 ≥ 32 字节 */
    @Value("${community.jwt.secret}")
    private String secret;

    /** token 有效期（毫秒），从配置读 */
    @Value("${community.jwt.expire}")
    private Long expire;

    /** 生成 token */
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(username)              // 主题：存用户名
                .claim("userId", userId)  // 自定义字段：用户ID
                .claim("role", role)      // 用户身份
                .issuedAt(new Date())           // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expire))  // 过期时间
                .signWith(getSecretKey())       // 用密钥签名
                .compact();                     // 生成紧凑字符串
    }

    /** 解析 token，返回 Claims（里面装了 userId/username/过期时间） */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())     // 用同一个密钥验证签名
                .build()
                .parseSignedClaims(token)       // 解析
                .getPayload();                  // 取出数据
    }

    /** 根据配置的 secret 字符串生成密钥 */
    public SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

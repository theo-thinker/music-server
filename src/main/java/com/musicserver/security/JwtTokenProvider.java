package com.musicserver.security;

import com.musicserver.common.Constants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT工具类
 * <p>
 * 使用jjwt 0.12.7最新API提供JWT令牌的生成、解析、验证等功能
 * 支持用户信息、权限、过期时间等处理
 *
 * @author Music Server Development Team
 * @version 2.0.0
 * @since 2025-09-01
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * JWT过期时间（秒）
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * JWT刷新过期时间（秒）
     */
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * 是否允许刷新令牌
     */
    @Value("${jwt.allow-refresh}")
    private boolean allowRefresh;

    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问令牌
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param authorities 权限列表
     * @return JWT访问令牌
     */
    public String generateAccessToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtExpiration, ChronoUnit.SECONDS);

        // 构建权限字符串
        String authoritiesString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 使用jjwt 0.12.7最新API
        return Jwts.builder()
                .subject(username)
                .claim(Constants.JWT_USER_ID_KEY, userId)
                .claim(Constants.JWT_USERNAME_KEY, username)
                .claim(Constants.JWT_AUTHORITIES_KEY, authoritiesString)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 生成刷新令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        if (!allowRefresh) {
            throw new UnsupportedOperationException("刷新令牌功能已禁用");
        }

        Instant now = Instant.now();
        Instant expiration = now.plus(refreshExpiration, ChronoUnit.SECONDS);

        // 使用jjwt 0.12.7最新API
        return Jwts.builder()
                .subject(username)
                .claim(Constants.JWT_USER_ID_KEY, userId)
                .claim(Constants.JWT_USERNAME_KEY, username)
                .claim("tokenType", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userIdObj = claims.get(Constants.JWT_USER_ID_KEY);

        if (userIdObj instanceof Number number) {
            return number.longValue();
        } else if (userIdObj instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException e) {
                log.warn("无法解析用户ID: {}", stringValue);
                return null;
            }
        }

        return null;
    }

    /**
     * 从令牌中获取权限列表
     *
     * @param token JWT令牌
     * @return 权限列表
     */
    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String authoritiesString = (String) claims.get(Constants.JWT_AUTHORITIES_KEY);

        if (authoritiesString == null || authoritiesString.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(authoritiesString.split(","))
                .map(String::trim)
                .filter(auth -> !auth.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 获取令牌过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 获取令牌签发时间
     *
     * @param token JWT令牌
     * @return 签发时间
     */
    public Date getIssuedAtFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getIssuedAt();
    }

    /**
     * 获取令牌过期时间（Instant格式）
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Instant getExpirationInstantFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration().toInstant();
    }

    /**
     * 获取令牌签发时间（Instant格式）
     *
     * @param token JWT令牌
     * @return 签发时间
     */
    public Instant getIssuedAtInstantFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getIssuedAt().toInstant();
    }

    /**
     * 验证令牌是否有效
     *
     * @param token    JWT令牌
     * @param username 用户名（可选，用于额外验证）
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenUsername = claims.getSubject();

            // 验证用户名匹配（如果提供）
            if (username != null && !username.equals(tokenUsername)) {
                log.warn("令牌用户名不匹配: expected={}, actual={}", username, tokenUsername);
                return false;
            }

            // 验证令牌是否过期
            if (isTokenExpired(token)) {
                log.warn("令牌已过期: username={}", tokenUsername);
                return false;
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证令牌是否有效（不验证用户名）
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        return validateToken(token, null);
    }

    /**
     * 判断令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Instant expiration = getExpirationInstantFromToken(token);
            return expiration.isBefore(Instant.now());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * 判断令牌是否可以刷新
     *
     * @param token JWT令牌
     * @return 是否可以刷新
     */
    public boolean canTokenBeRefreshed(String token) {
        if (!allowRefresh) {
            return false;
        }

        try {
            // 检查是否为刷新令牌
            Claims claims = getClaimsFromToken(token);
            String tokenType = (String) claims.get("tokenType");

            if (!"refresh".equals(tokenType)) {
                return false;
            }

            // 检查是否过期
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    public String refreshAccessToken(String refreshToken) {
        if (!canTokenBeRefreshed(refreshToken)) {
            throw new JwtException("无法刷新令牌");
        }

        try {
            Claims claims = getClaimsFromToken(refreshToken);
            String username = claims.getSubject();
            Long userId = getUserIdFromToken(refreshToken);

            // 这里需要从数据库重新获取用户权限
            // 暂时使用空权限列表，实际应该调用UserDetailsService
            Collection<GrantedAuthority> authorities = Collections.emptyList();

            return generateAccessToken(userId, username, authorities);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 从令牌中获取Claims
     * 使用jjwt 0.12.7最新API
     *
     * @param token JWT令牌
     * @return Claims对象
     * @throws JwtException 令牌解析异常
     */
    private Claims getClaimsFromToken(String token) {
        try {
            // 使用jjwt 0.12.7最新的parser API
            JwtParser parser = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .clockSkewSeconds(60) // 允许60秒的时钟偏差
                    .build();

            return parser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT令牌已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT令牌格式错误: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("JWT签名验证失败: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌参数错误: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("JWT令牌处理异常: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 从请求头中提取令牌
     *
     * @param authHeader 授权请求头
     * @return JWT令牌，如果没有有效令牌则返回null
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(Constants.JWT_TOKEN_PREFIX)) {
            return authHeader.substring(Constants.JWT_TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 获取令牌剩余有效时间（秒）
     *
     * @param token JWT令牌
     * @return 剩余有效时间（秒），如果令牌无效或已过期返回0
     */
    public long getRemainingValidityTime(String token) {
        try {
            Instant expiration = getExpirationInstantFromToken(token);
            Instant now = Instant.now();
            long remaining = ChronoUnit.SECONDS.between(now, expiration);
            return Math.max(0, remaining);
        } catch (JwtException | IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * 获取令牌的唯一标识（JTI）
     *
     * @param token JWT令牌
     * @return JTI标识，如果不存在返回null
     */
    public String getJtiFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getId();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("无法获取令牌JTI: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成带JTI的令牌
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param authorities 权限列表
     * @param jti         令牌唯一标识
     * @return JWT访问令牌
     */
    public String generateAccessTokenWithJti(Long userId, String username,
                                             Collection<? extends GrantedAuthority> authorities, String jti) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtExpiration, ChronoUnit.SECONDS);

        // 构建权限字符串
        String authoritiesString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 使用jjwt 0.12.7最新API
        return Jwts.builder()
                .id(jti)
                .subject(username)
                .claim(Constants.JWT_USER_ID_KEY, userId)
                .claim(Constants.JWT_USERNAME_KEY, username)
                .claim(Constants.JWT_AUTHORITIES_KEY, authoritiesString)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        // 这里应该检查Redis或数据库中的黑名单
        // 暂时返回false，实际应该注入BlacklistService
        return false;
    }

    /**
     * 创建无状态JWT解析器
     * 使用jjwt 0.12.7最新API，可复用以提高性能
     *
     * @return JWT解析器
     */
    private JwtParser createParser() {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .clockSkewSeconds(60) // 允许60秒的时钟偏差
                .build();
    }

    /**
     * 验证令牌完整性（不抛出异常）
     *
     * @param token JWT令牌
     * @return 验证结果信息
     */
    public TokenValidationResult validateTokenSafely(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String username = claims.getSubject();
            Instant expiration = claims.getExpiration().toInstant();

            if (expiration.isBefore(Instant.now())) {
                return new TokenValidationResult(false, "令牌已过期", username);
            }

            if (isTokenBlacklisted(token)) {
                return new TokenValidationResult(false, "令牌已被列入黑名单", username);
            }

            return new TokenValidationResult(true, "令牌有效", username);
        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(false, "令牌已过期", e.getClaims().getSubject());
        } catch (MalformedJwtException e) {
            return new TokenValidationResult(false, "令牌格式错误", null);
        } catch (SignatureException e) {
            return new TokenValidationResult(false, "令牌签名验证失败", null);
        } catch (JwtException e) {
            return new TokenValidationResult(false, "令牌无效: " + e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new TokenValidationResult(false, "令牌参数错误", null);
        }
    }

    /**
     * 令牌验证结果
     */
    @Getter
    public static class TokenValidationResult {
        private final boolean valid;
        private final String message;
        private final String username;

        public TokenValidationResult(boolean valid, String message, String username) {
            this.valid = valid;
            this.message = message;
            this.username = username;
        }

    }

    /**
     * 生成JWT令牌构建器
     * 使用Builder模式提高代码可读性
     *
     * @return JWT令牌构建器
     */
    public TokenBuilder tokenBuilder() {
        return new TokenBuilder();
    }

    /**
     * JWT令牌构建器
     */
    public class TokenBuilder {
        private String subject;
        private Long userId;
        private String username;
        private Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
        private String jti;
        private String tokenType = "access";
        private Instant issuedAt = Instant.now();
        private Instant expiration;
        private final Map<String, Object> claims = new HashMap<>();

        public TokenBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public TokenBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public TokenBuilder username(String username) {
            this.username = username;
            this.subject = username; // 默认使用username作为subject
            return this;
        }

        public TokenBuilder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public TokenBuilder jti(String jti) {
            this.jti = jti;
            return this;
        }

        public TokenBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public TokenBuilder issuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public TokenBuilder expiration(Instant expiration) {
            this.expiration = expiration;
            return this;
        }

        public TokenBuilder expiresInSeconds(long seconds) {
            this.expiration = issuedAt.plus(seconds, ChronoUnit.SECONDS);
            return this;
        }

        public TokenBuilder claim(String name, Object value) {
            this.claims.put(name, value);
            return this;
        }

        public String build() {
            if (subject == null) {
                throw new IllegalArgumentException("Subject不能为空");
            }

            if (expiration == null) {
                // 根据令牌类型设置默认过期时间
                long defaultExpiration = "refresh".equals(tokenType) ? refreshExpiration : jwtExpiration;
                expiration = issuedAt.plus(defaultExpiration, ChronoUnit.SECONDS);
            }

            JwtBuilder builder = Jwts.builder()
                    .subject(subject)
                    .issuedAt(Date.from(issuedAt))
                    .expiration(Date.from(expiration))
                    .signWith(getSigningKey(), Jwts.SIG.HS256);

            if (jti != null) {
                builder.id(jti);
            }

            if (userId != null) {
                builder.claim(Constants.JWT_USER_ID_KEY, userId);
            }

            if (username != null) {
                builder.claim(Constants.JWT_USERNAME_KEY, username);
            }

            if (authorities != null && !authorities.isEmpty()) {
                String authoritiesString = authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
                builder.claim(Constants.JWT_AUTHORITIES_KEY, authoritiesString);
            }

            if (tokenType != null) {
                builder.claim("tokenType", tokenType);
            }

            // 添加自定义claims
            claims.forEach(builder::claim);

            return builder.compact();
        }
    }
}
package com.musicserver.security;

import com.musicserver.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 
 * 继承OncePerRequestFilter，确保每个请求只执行一次过滤
 * 从请求头中提取JWT令牌，验证并设置安全上下文
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT工具类
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户详情服务
     */
    private final UserDetailsService userDetailsService;

    /**
     * 执行过滤逻辑
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求头中提取JWT令牌
            String jwt = getJwtFromRequest(request);
            
            // 如果令牌存在且有效，设置安全上下文
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 从令牌中获取用户名
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                
                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                
                // 设置认证详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 设置安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("设置安全上下文成功，用户: {}", username);
            } else if (StringUtils.hasText(jwt)) {
                log.warn("JWT令牌验证失败: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
            }
            
        } catch (Exception ex) {
            log.error("无法设置用户认证: {}", ex.getMessage());
            // 清除安全上下文
            SecurityContextHolder.clearContext();
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求中提取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌，如果不存在返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 从Authorization头中获取令牌
        String bearerToken = request.getHeader(Constants.JWT_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.JWT_TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.JWT_TOKEN_PREFIX.length());
        }
        
        // 也可以从请求参数中获取令牌（可选）
        String paramToken = request.getParameter("token");
        if (StringUtils.hasText(paramToken)) {
            return paramToken;
        }
        
        return null;
    }

    /**
     * 判断是否需要跳过过滤器
     * 可以在此方法中定义不需要认证的URL路径
     * 
     * @param request HTTP请求
     * @return true-跳过过滤器，false-执行过滤器
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳过公开API路径
        String[] publicPaths = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/captcha",
            "/doc.html",
            "/webjars/",
            "/swagger-resources/",
            "/v3/api-docs/",
            "/swagger-ui/",
            "/actuator/",
            "/druid/",
            "/static/",
            "/favicon.ico"
        };
        
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        
        return false;
    }
}
package com.musicserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicserver.common.Result;
import com.musicserver.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT认证异常处理器
 * 
 * 实现AuthenticationEntryPoint接口，处理认证失败的情况
 * 当用户未认证或认证失败时，返回统一的JSON错误响应
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * JSON序列化器
     */
    private final ObjectMapper objectMapper;

    /**
     * 处理认证异常
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param authException 认证异常
     * @throws IOException IO异常
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = getClientIpAddress(request);
        
        log.warn("认证失败 - URI: {} {}, IP: {}, 异常: {}", 
                 method, requestUri, clientIp, authException.getMessage());

        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 创建错误响应
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), "认证失败，请重新登录");
        
        // 序列化并写入响应
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
package com.musicserver.websocket;

import com.musicserver.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

/**
 * WebSocket认证拦截器
 * 
 * 拦截WebSocket连接，验证JWT令牌
 * 为已认证用户设置Principal
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 在消息发送到通道之前进行拦截
     * 
     * @param message 消息
     * @param channel 通道
     * @return 处理后的消息
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 处理WebSocket连接请求
            authenticateUser(accessor);
        }
        
        return message;
    }

    /**
     * 认证用户
     * 
     * @param accessor STOMP头访问器
     */
    private void authenticateUser(StompHeaderAccessor accessor) {
        try {
            // 从头部获取认证令牌
            String token = extractToken(accessor);
            
            if (StringUtils.hasText(token)) {
                // 验证令牌并提取用户信息
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    
                    // 创建认证主体
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_USER"));
                    
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    
                    // 设置用户Principal
                    accessor.setUser(new WebSocketUserPrincipal(userId, username, token));
                    
                    log.debug("WebSocket用户认证成功: userId={}, username={}", userId, username);
                } else {
                    log.warn("WebSocket认证失败: 无效的JWT令牌");
                    throw new IllegalArgumentException("无效的认证令牌");
                }
            } else {
                log.warn("WebSocket认证失败: 缺少认证令牌");
                throw new IllegalArgumentException("缺少认证令牌");
            }
        } catch (Exception e) {
            log.error("WebSocket认证处理异常", e);
            throw new IllegalArgumentException("认证失败: " + e.getMessage());
        }
    }

    /**
     * 从STOMP头部提取JWT令牌
     * 
     * @param accessor STOMP头访问器
     * @return JWT令牌
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // 尝试从Authorization头获取令牌
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        
        // 尝试从自定义头获取令牌
        List<String> tokenHeaders = accessor.getNativeHeader("X-Auth-Token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }
        
        // 尝试从query参数获取令牌 (SockJS连接)
        List<String> tokenParams = accessor.getNativeHeader("token");
        if (tokenParams != null && !tokenParams.isEmpty()) {
            return tokenParams.get(0);
        }
        
        return null;
    }

    /**
     * WebSocket用户Principal实现
     */
    public static class WebSocketUserPrincipal implements Principal {
        private final Long userId;
        private final String username;
        private final String token;

        public WebSocketUserPrincipal(Long userId, String username, String token) {
            this.userId = userId;
            this.username = username;
            this.token = token;
        }

        @Override
        public String getName() {
            return username;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getToken() {
            return token;
        }

        @Override
        public String toString() {
            return "WebSocketUserPrincipal{" +
                    "userId=" + userId +
                    ", username='" + username + '\'' +
                    '}';
        }
    }
}
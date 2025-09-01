package com.musicserver.websocket;

import com.musicserver.websocket.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;

/**
 * WebSocket事件监听器
 * 
 * 监听WebSocket连接、断开、订阅、取消订阅事件
 * 维护用户在线状态和会话管理
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final OnlineUserService onlineUserService;

    /**
     * 处理WebSocket连接事件
     * 
     * @param event 连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal user = headerAccessor.getUser();
        
        if (user instanceof WebSocketAuthInterceptor.WebSocketUserPrincipal userPrincipal) {
            Long userId = userPrincipal.getUserId();
            String username = userPrincipal.getUsername();
            
            // 添加用户到在线列表
            onlineUserService.addOnlineUser(userId, username, sessionId);
            
            log.info("WebSocket连接成功: sessionId={}, userId={}, username={}", 
                    sessionId, userId, username);
            
            // 广播用户上线通知
            onlineUserService.broadcastUserOnline(userId, username);
        } else {
            log.warn("WebSocket连接成功但用户信息缺失: sessionId={}", sessionId);
        }
    }

    /**
     * 处理WebSocket断开事件
     * 
     * @param event 断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal user = headerAccessor.getUser();
        
        if (user instanceof WebSocketAuthInterceptor.WebSocketUserPrincipal userPrincipal) {
            Long userId = userPrincipal.getUserId();
            String username = userPrincipal.getUsername();
            
            // 从在线列表移除用户
            onlineUserService.removeOnlineUser(userId, sessionId);
            
            log.info("WebSocket连接断开: sessionId={}, userId={}, username={}", 
                    sessionId, userId, username);
            
            // 广播用户下线通知
            onlineUserService.broadcastUserOffline(userId, username);
        } else {
            log.info("WebSocket连接断开: sessionId={}", sessionId);
        }
    }

    /**
     * 处理WebSocket订阅事件
     * 
     * @param event 订阅事件
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        Principal user = headerAccessor.getUser();
        
        if (user instanceof WebSocketAuthInterceptor.WebSocketUserPrincipal userPrincipal) {
            Long userId = userPrincipal.getUserId();
            String username = userPrincipal.getUsername();
            
            log.debug("用户订阅频道: userId={}, username={}, sessionId={}, destination={}", 
                    userId, username, sessionId, destination);
            
            // 处理特定频道的订阅逻辑
            handleChannelSubscription(userId, username, sessionId, destination);
        }
    }

    /**
     * 处理WebSocket取消订阅事件
     * 
     * @param event 取消订阅事件
     */
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String subscriptionId = headerAccessor.getSubscriptionId();
        Principal user = headerAccessor.getUser();
        
        if (user instanceof WebSocketAuthInterceptor.WebSocketUserPrincipal userPrincipal) {
            Long userId = userPrincipal.getUserId();
            String username = userPrincipal.getUsername();
            
            log.debug("用户取消订阅: userId={}, username={}, sessionId={}, subscriptionId={}", 
                    userId, username, sessionId, subscriptionId);
            
            // 处理特定频道的取消订阅逻辑
            handleChannelUnsubscription(userId, username, sessionId, subscriptionId);
        }
    }

    /**
     * 处理频道订阅逻辑
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param sessionId 会话ID
     * @param destination 目标频道
     */
    private void handleChannelSubscription(Long userId, String username, String sessionId, String destination) {
        if (destination == null) {
            return;
        }
        
        try {
            // 处理音乐播放状态频道订阅
            if (destination.startsWith("/topic/music/")) {
                handleMusicChannelSubscription(userId, username, sessionId, destination);
            }
            // 处理聊天室频道订阅
            else if (destination.startsWith("/topic/chat/")) {
                handleChatChannelSubscription(userId, username, sessionId, destination);
            }
            // 处理用户个人频道订阅
            else if (destination.startsWith("/user/queue/")) {
                handleUserChannelSubscription(userId, username, sessionId, destination);
            }
            // 处理在线用户列表订阅
            else if ("/topic/online/users".equals(destination)) {
                onlineUserService.sendOnlineUsersToUser(userId);
            }
        } catch (Exception e) {
            log.error("处理频道订阅失败: userId={}, destination={}", userId, destination, e);
        }
    }

    /**
     * 处理频道取消订阅逻辑
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param sessionId 会话ID
     * @param subscriptionId 订阅ID
     */
    private void handleChannelUnsubscription(Long userId, String username, String sessionId, String subscriptionId) {
        try {
            // 这里可以添加取消订阅的特殊处理逻辑
            log.debug("用户取消订阅处理完成: userId={}, subscriptionId={}", userId, subscriptionId);
        } catch (Exception e) {
            log.error("处理频道取消订阅失败: userId={}, subscriptionId={}", userId, subscriptionId, e);
        }
    }

    /**
     * 处理音乐频道订阅
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param sessionId 会话ID
     * @param destination 目标频道
     */
    private void handleMusicChannelSubscription(Long userId, String username, String sessionId, String destination) {
        // 提取房间ID或播放列表ID
        String[] parts = destination.split("/");
        if (parts.length >= 4) {
            String roomId = parts[3];
            log.debug("用户加入音乐房间: userId={}, roomId={}", userId, roomId);
            
            // 这里可以添加加入音乐房间的逻辑
            // 例如：发送当前播放状态给新加入的用户
        }
    }

    /**
     * 处理聊天频道订阅
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param sessionId 会话ID
     * @param destination 目标频道
     */
    private void handleChatChannelSubscription(Long userId, String username, String sessionId, String destination) {
        // 提取聊天室ID
        String[] parts = destination.split("/");
        if (parts.length >= 4) {
            String chatRoomId = parts[3];
            log.debug("用户加入聊天室: userId={}, chatRoomId={}", userId, chatRoomId);
            
            // 这里可以添加加入聊天室的逻辑
            // 例如：通知其他用户有新成员加入
        }
    }

    /**
     * 处理用户个人频道订阅
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param sessionId 会话ID
     * @param destination 目标频道
     */
    private void handleUserChannelSubscription(Long userId, String username, String sessionId, String destination) {
        log.debug("用户订阅个人频道: userId={}, destination={}", userId, destination);
        
        // 用户订阅个人频道时，可以发送一些个人相关的信息
        // 例如：未读消息、好友状态等
    }
}
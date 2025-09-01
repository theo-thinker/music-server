package com.musicserver.websocket.service;

import com.musicserver.dto.websocket.UserOnlineStatusMessage;
import com.musicserver.dto.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户在线状态管理服务
 * 
 * 管理用户在线状态、会话信息
 * 提供在线用户查询、状态广播等功能
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    // 在线用户缓存 userId -> UserSession
    private final Map<Long, UserSession> onlineUsers = new ConcurrentHashMap<>();
    
    // 会话映射 sessionId -> userId
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    private static final String ONLINE_USERS_KEY = "websocket:online:users";
    private static final String USER_SESSION_KEY = "websocket:session:";
    private static final long SESSION_TIMEOUT = 30; // 会话超时时间（分钟）

    /**
     * 添加用户到在线列表
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param sessionId 会话ID
     */
    public void addOnlineUser(Long userId, String username, String sessionId) {
        try {
            UserSession session = UserSession.builder()
                    .userId(userId)
                    .username(username)
                    .sessionId(sessionId)
                    .status("ONLINE")
                    .loginTime(LocalDateTime.now())
                    .lastActiveTime(LocalDateTime.now())
                    .deviceType("WEB")
                    .build();

            // 添加到内存缓存
            onlineUsers.put(userId, session);
            sessionUserMap.put(sessionId, userId);

            // 同步到Redis
            String redisKey = USER_SESSION_KEY + userId;
            redisTemplate.opsForValue().set(redisKey, session, SESSION_TIMEOUT, TimeUnit.MINUTES);
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);

            log.info("用户上线: userId={}, username={}, sessionId={}", userId, username, sessionId);
        } catch (Exception e) {
            log.error("添加在线用户失败: userId={}", userId, e);
        }
    }

    /**
     * 从在线列表移除用户
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    public void removeOnlineUser(Long userId, String sessionId) {
        try {
            // 从内存缓存移除
            UserSession session = onlineUsers.remove(userId);
            sessionUserMap.remove(sessionId);

            // 从Redis移除
            String redisKey = USER_SESSION_KEY + userId;
            redisTemplate.delete(redisKey);
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);

            if (session != null) {
                log.info("用户下线: userId={}, username={}, sessionId={}", 
                        userId, session.getUsername(), sessionId);
            }
        } catch (Exception e) {
            log.error("移除在线用户失败: userId={}", userId, e);
        }
    }

    /**
     * 更新用户活跃时间
     * 
     * @param userId 用户ID
     */
    public void updateUserActivity(Long userId) {
        try {
            UserSession session = onlineUsers.get(userId);
            if (session != null) {
                session.setLastActiveTime(LocalDateTime.now());
                
                // 更新Redis
                String redisKey = USER_SESSION_KEY + userId;
                redisTemplate.opsForValue().set(redisKey, session, SESSION_TIMEOUT, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("更新用户活跃时间失败: userId={}", userId, e);
        }
    }

    /**
     * 获取在线用户列表
     * 
     * @return 在线用户列表
     */
    public List<UserOnlineStatusMessage> getOnlineUsers() {
        try {
            return onlineUsers.values().stream()
                    .map(this::convertToStatusMessage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取在线用户数量
     * 
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    /**
     * 获取用户会话信息
     * 
     * @param userId 用户ID
     * @return 用户会话信息
     */
    public UserSession getUserSession(Long userId) {
        return onlineUsers.get(userId);
    }

    /**
     * 根据会话ID获取用户ID
     * 
     * @param sessionId 会话ID
     * @return 用户ID
     */
    public Long getUserIdBySession(String sessionId) {
        return sessionUserMap.get(sessionId);
    }

    /**
     * 广播用户上线通知
     * 
     * @param userId 用户ID
     * @param username 用户名
     */
    public void broadcastUserOnline(Long userId, String username) {
        try {
            UserOnlineStatusMessage statusMessage = UserOnlineStatusMessage.builder()
                    .userId(userId)
                    .username(username)
                    .status("ONLINE")
                    .statusTime(LocalDateTime.now())
                    .lastActiveTime(LocalDateTime.now())
                    .deviceType("WEB")
                    .build();

            WebSocketMessage message = WebSocketMessage.builder()
                    .type("USER_ONLINE")
                    .content(statusMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            // 广播给所有在线用户
            messagingTemplate.convertAndSend("/topic/online/status", message);
            
            log.debug("广播用户上线通知: userId={}, username={}", userId, username);
        } catch (Exception e) {
            log.error("广播用户上线通知失败: userId={}", userId, e);
        }
    }

    /**
     * 广播用户下线通知
     * 
     * @param userId 用户ID
     * @param username 用户名
     */
    public void broadcastUserOffline(Long userId, String username) {
        try {
            UserOnlineStatusMessage statusMessage = UserOnlineStatusMessage.builder()
                    .userId(userId)
                    .username(username)
                    .status("OFFLINE")
                    .statusTime(LocalDateTime.now())
                    .lastActiveTime(LocalDateTime.now())
                    .build();

            WebSocketMessage message = WebSocketMessage.builder()
                    .type("USER_OFFLINE")
                    .content(statusMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            // 广播给所有在线用户
            messagingTemplate.convertAndSend("/topic/online/status", message);
            
            log.debug("广播用户下线通知: userId={}, username={}", userId, username);
        } catch (Exception e) {
            log.error("广播用户下线通知失败: userId={}", userId, e);
        }
    }

    /**
     * 发送在线用户列表给指定用户
     * 
     * @param userId 用户ID
     */
    public void sendOnlineUsersToUser(Long userId) {
        try {
            List<UserOnlineStatusMessage> onlineUsersList = getOnlineUsers();
            
            WebSocketMessage message = WebSocketMessage.builder()
                    .type("ONLINE_USERS_LIST")
                    .content(onlineUsersList)
                    .timestamp(LocalDateTime.now())
                    .build();

            // 发送给指定用户
            messagingTemplate.convertAndSendToUser(
                    userId.toString(), 
                    "/queue/online/users", 
                    message
            );
            
            log.debug("发送在线用户列表给用户: userId={}, count={}", userId, onlineUsersList.size());
        } catch (Exception e) {
            log.error("发送在线用户列表失败: userId={}", userId, e);
        }
    }

    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 状态
     */
    public void updateUserStatus(Long userId, String status) {
        try {
            UserSession session = onlineUsers.get(userId);
            if (session != null) {
                session.setStatus(status);
                session.setLastActiveTime(LocalDateTime.now());
                
                // 更新Redis
                String redisKey = USER_SESSION_KEY + userId;
                redisTemplate.opsForValue().set(redisKey, session, SESSION_TIMEOUT, TimeUnit.MINUTES);
                
                // 广播状态变化
                broadcastUserStatusChange(userId, session.getUsername(), status);
            }
        } catch (Exception e) {
            log.error("更新用户状态失败: userId={}, status={}", userId, status, e);
        }
    }

    /**
     * 广播用户状态变化
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param status 状态
     */
    private void broadcastUserStatusChange(Long userId, String username, String status) {
        try {
            UserOnlineStatusMessage statusMessage = UserOnlineStatusMessage.builder()
                    .userId(userId)
                    .username(username)
                    .status(status)
                    .statusTime(LocalDateTime.now())
                    .build();

            WebSocketMessage message = WebSocketMessage.builder()
                    .type("USER_STATUS_CHANGE")
                    .content(statusMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/online/status", message);
        } catch (Exception e) {
            log.error("广播用户状态变化失败: userId={}", userId, e);
        }
    }

    /**
     * 清理过期会话
     */
    public void cleanExpiredSessions() {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT);
            List<Long> expiredUsers = onlineUsers.values().stream()
                    .filter(session -> session.getLastActiveTime().isBefore(expireTime))
                    .map(UserSession::getUserId)
                    .collect(Collectors.toList());

            for (Long userId : expiredUsers) {
                UserSession session = onlineUsers.get(userId);
                if (session != null) {
                    removeOnlineUser(userId, session.getSessionId());
                    broadcastUserOffline(userId, session.getUsername());
                }
            }

            if (!expiredUsers.isEmpty()) {
                log.info("清理过期会话: count={}", expiredUsers.size());
            }
        } catch (Exception e) {
            log.error("清理过期会话失败", e);
        }
    }

    /**
     * 转换为状态消息
     * 
     * @param session 用户会话
     * @return 状态消息
     */
    private UserOnlineStatusMessage convertToStatusMessage(UserSession session) {
        return UserOnlineStatusMessage.builder()
                .userId(session.getUserId())
                .username(session.getUsername())
                .status(session.getStatus())
                .statusTime(LocalDateTime.now())
                .lastActiveTime(session.getLastActiveTime())
                .deviceType(session.getDeviceType())
                .sessionId(session.getSessionId())
                .build();
    }

    /**
     * 用户会话信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserSession {
        private Long userId;
        private String username;
        private String sessionId;
        private String status;
        private LocalDateTime loginTime;
        private LocalDateTime lastActiveTime;
        private String deviceType;
        private String ipAddress;
        private String location;
        private Long currentMusicId;
        private String currentMusicTitle;
        private Boolean isPlaying;
        private String roomId;
    }
}
package com.musicserver.websocket.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicserver.dto.websocket.WebSocketMessage;
import com.musicserver.websocket.constant.WebSocketConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket工具类
 * 
 * 提供WebSocket消息发送、格式化等便捷方法
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketUtil {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发送消息给指定用户
     * 
     * @param userId 用户ID
     * @param destination 目标队列
     * @param message 消息内容
     */
    public void sendToUser(Long userId, String destination, Object message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    destination,
                    message
            );
            
            log.debug("发送消息给用户: userId={}, destination={}", userId, destination);
        } catch (Exception e) {
            log.error("发送消息给用户失败: userId={}, destination={}", userId, destination, e);
        }
    }

    /**
     * 发送WebSocket消息给指定用户
     * 
     * @param userId 用户ID
     * @param destination 目标队列
     * @param type 消息类型
     * @param content 消息内容
     */
    public void sendWebSocketMessage(Long userId, String destination, String type, Object content) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type(type)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        
        sendToUser(userId, destination, message);
    }

    /**
     * 广播消息给所有用户
     * 
     * @param destination 目标主题
     * @param message 消息内容
     */
    public void broadcast(String destination, Object message) {
        try {
            messagingTemplate.convertAndSend(destination, message);
            
            log.debug("广播消息: destination={}", destination);
        } catch (Exception e) {
            log.error("广播消息失败: destination={}", destination, e);
        }
    }

    /**
     * 广播WebSocket消息给所有用户
     * 
     * @param destination 目标主题
     * @param type 消息类型
     * @param content 消息内容
     */
    public void broadcastWebSocketMessage(String destination, String type, Object content) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type(type)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        
        broadcast(destination, message);
    }

    /**
     * 发送成功响应
     * 
     * @param userId 用户ID
     * @param destination 目标队列
     * @param type 消息类型
     * @param content 消息内容
     */
    public void sendSuccessResponse(Long userId, String destination, String type, Object content) {
        WebSocketMessage message = WebSocketMessage.success(type, content);
        sendToUser(userId, destination, message);
    }

    /**
     * 发送错误响应
     * 
     * @param userId 用户ID
     * @param destination 目标队列
     * @param type 消息类型
     * @param errorMessage 错误信息
     */
    public void sendErrorResponse(Long userId, String destination, String type, String errorMessage) {
        WebSocketMessage message = WebSocketMessage.error(type, errorMessage);
        sendToUser(userId, destination, message);
    }

    /**
     * 发送心跳响应
     * 
     * @param userId 用户ID
     */
    public void sendHeartbeatResponse(Long userId) {
        sendWebSocketMessage(
                userId,
                WebSocketConstants.Destinations.QUEUE_HEARTBEAT,
                WebSocketConstants.MessageType.HEARTBEAT_RESPONSE,
                "pong"
        );
    }

    /**
     * 发送系统通知
     * 
     * @param userId 用户ID（null表示广播给所有用户）
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @param level 通知级别
     */
    public void sendSystemNotification(Long userId, String title, String content, 
                                     String type, String level) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("notificationId", "sys_" + System.currentTimeMillis());
        notification.put("notificationType", type);
        notification.put("title", title);
        notification.put("content", content);
        notification.put("level", level);
        notification.put("notificationTime", LocalDateTime.now());
        if (userId != null) {
            notification.put("targetUserId", userId);
        }

        if (userId != null) {
            sendWebSocketMessage(
                    userId,
                    WebSocketConstants.Destinations.QUEUE_NOTIFICATIONS,
                    WebSocketConstants.MessageType.USER_NOTIFICATION,
                    notification
            );
        } else {
            broadcastWebSocketMessage(
                    WebSocketConstants.Destinations.TOPIC_SYSTEM_NOTIFICATIONS,
                    WebSocketConstants.MessageType.SYSTEM_NOTIFICATION,
                    notification
            );
        }
    }

    /**
     * 发送强制下线通知
     * 
     * @param userId 用户ID
     * @param reason 下线原因
     */
    public void sendForceLogoutNotification(Long userId, String reason) {
        Map<String, Object> notification = Map.of(
                "notificationId", "logout_" + System.currentTimeMillis(),
                "notificationType", WebSocketConstants.NotificationType.FORCE_LOGOUT,
                "title", "账号异常",
                "content", reason != null ? reason : "您的账号在其他地方登录，已被强制下线",
                "level", WebSocketConstants.NotificationLevel.WARNING,
                "notificationTime", LocalDateTime.now(),
                "targetUserId", userId
        );

        sendWebSocketMessage(
                userId,
                WebSocketConstants.Destinations.QUEUE_SYSTEM_FORCE_LOGOUT,
                WebSocketConstants.MessageType.FORCE_LOGOUT,
                notification
        );
    }

    /**
     * 验证消息格式
     * 
     * @param message 消息内容
     * @return 是否有效
     */
    public boolean isValidMessage(Object message) {
        if (message == null) {
            return false;
        }

        try {
            String jsonString = objectMapper.writeValueAsString(message);
            return jsonString.length() <= WebSocketConstants.Limits.MAX_MESSAGE_LENGTH;
        } catch (Exception e) {
            log.warn("消息格式验证失败", e);
            return false;
        }
    }

    /**
     * 构建房间频道地址
     * 
     * @param roomId 房间ID
     * @return 频道地址
     */
    public String buildRoomDestination(String roomId) {
        return WebSocketConstants.Destinations.TOPIC_MUSIC_ROOM + roomId;
    }

    /**
     * 构建聊天室频道地址
     * 
     * @param chatRoomId 聊天室ID
     * @return 频道地址
     */
    public String buildChatRoomDestination(String chatRoomId) {
        return WebSocketConstants.Destinations.TOPIC_CHAT_ROOM + chatRoomId;
    }

    /**
     * 解析房间ID从频道地址
     * 
     * @param destination 频道地址
     * @return 房间ID
     */
    public String extractRoomIdFromDestination(String destination) {
        if (destination.startsWith(WebSocketConstants.Destinations.TOPIC_MUSIC_ROOM)) {
            return destination.substring(WebSocketConstants.Destinations.TOPIC_MUSIC_ROOM.length());
        }
        return null;
    }

    /**
     * 解析聊天室ID从频道地址
     * 
     * @param destination 频道地址
     * @return 聊天室ID
     */
    public String extractChatRoomIdFromDestination(String destination) {
        if (destination.startsWith(WebSocketConstants.Destinations.TOPIC_CHAT_ROOM)) {
            return destination.substring(WebSocketConstants.Destinations.TOPIC_CHAT_ROOM.length());
        }
        return null;
    }

    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的大小
     */
    public String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", 
                size / Math.pow(1024, digitGroups), 
                units[digitGroups]);
    }

    /**
     * 生成唯一消息ID
     * 
     * @param prefix 前缀
     * @param userId 用户ID
     * @return 消息ID
     */
    public String generateMessageId(String prefix, Long userId) {
        return String.format("%s_%d_%d", prefix, System.currentTimeMillis(), userId);
    }

    /**
     * 检查用户权限
     * 
     * @param userId 用户ID
     * @param permission 权限
     * @return 是否有权限
     */
    public boolean hasPermission(Long userId, String permission) {
        // 这里可以集成具体的权限检查逻辑
        // 暂时返回true，实际项目中应该检查用户角色和权限
        return true;
    }

    /**
     * 获取客户端IP地址
     * 
     * @param sessionAttributes 会话属性
     * @return IP地址
     */
    public String getClientIpAddress(Map<String, Object> sessionAttributes) {
        // 从会话属性中获取IP地址
        Object remoteAddress = sessionAttributes.get("REMOTE_ADDRESS");
        if (remoteAddress != null) {
            return remoteAddress.toString();
        }
        return "unknown";
    }

    /**
     * 获取设备类型
     * 
     * @param userAgent 用户代理字符串
     * @return 设备类型
     */
    public String getDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return WebSocketConstants.DeviceType.UNKNOWN;
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return WebSocketConstants.DeviceType.MOBILE;
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return WebSocketConstants.DeviceType.TABLET;
        } else if (userAgent.contains("electron") || userAgent.contains("desktop")) {
            return WebSocketConstants.DeviceType.DESKTOP;
        } else {
            return WebSocketConstants.DeviceType.WEB;
        }
    }

    /**
     * 记录WebSocket操作日志
     * 
     * @param operation 操作类型
     * @param userId 用户ID
     * @param details 详细信息
     */
    public void logWebSocketOperation(String operation, Long userId, String details) {
        log.info("WebSocket操作: operation={}, userId={}, details={}", operation, userId, details);
    }

    /**
     * 记录WebSocket错误日志
     * 
     * @param operation 操作类型
     * @param userId 用户ID
     * @param error 错误信息
     * @param exception 异常对象
     */
    public void logWebSocketError(String operation, Long userId, String error, Exception exception) {
        log.error("WebSocket错误: operation={}, userId={}, error={}", operation, userId, error, exception);
    }
}
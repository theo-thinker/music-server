package com.musicserver.websocket.handler;

import com.musicserver.dto.websocket.*;
import com.musicserver.websocket.WebSocketAuthInterceptor;
import com.musicserver.websocket.service.MusicPlayService;
import com.musicserver.websocket.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket消息处理器
 * 
 * 处理客户端发送的WebSocket消息
 * 包括音乐播放控制、聊天消息、状态更新等
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketMessageHandler {

    private final MusicPlayService musicPlayService;
    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 处理音乐播放控制消息
     * 
     * @param message 播放控制消息
     * @param principal 用户身份
     * @return 播放状态消息
     */
    @MessageMapping("/music/control")
    @SendToUser("/queue/music/status")
    public WebSocketMessage handleMusicControl(@Payload MusicPlayStatusMessage message, Principal principal) {
        try {
            WebSocketAuthInterceptor.WebSocketUserPrincipal user = 
                    (WebSocketAuthInterceptor.WebSocketUserPrincipal) principal;
            
            Long userId = user.getUserId();
            
            // 更新用户播放状态
            musicPlayService.updateUserPlayStatus(
                    userId,
                    message.getMusicId(),
                    message.getAction(),
                    message.getCurrentTime(),
                    message.getVolume(),
                    message.getPlayMode()
            );
            
            // 更新用户活跃时间
            onlineUserService.updateUserActivity(userId);
            
            log.debug("处理音乐播放控制: userId={}, action={}, musicId={}", 
                    userId, message.getAction(), message.getMusicId());
            
            return WebSocketMessage.success("MUSIC_CONTROL_SUCCESS", "音乐控制成功");
            
        } catch (Exception e) {
            log.error("处理音乐播放控制失败", e);
            return WebSocketMessage.error("MUSIC_CONTROL_ERROR", "音乐控制失败: " + e.getMessage());
        }
    }

    /**
     * 处理房间音乐控制消息（仅房主）
     * 
     * @param roomId 房间ID
     * @param message 播放控制消息
     * @param principal 用户身份
     */
    @MessageMapping("/music/room/{roomId}/control")
    public void handleRoomMusicControl(@DestinationVariable String roomId,
                                     @Payload MusicPlayStatusMessage message,
                                     Principal principal) {
        try {
            WebSocketAuthInterceptor.WebSocketUserPrincipal user = 
                    (WebSocketAuthInterceptor.WebSocketUserPrincipal) principal;
            
            Long userId = user.getUserId();
            
            // 房间音乐控制
            musicPlayService.controlRoomMusic(
                    userId,
                    roomId,
                    message.getMusicId(),
                    message.getAction(),
                    message.getCurrentTime(),
                    message.getVolume(),
                    message.getPlayMode()
            );
            
            log.debug("处理房间音乐控制: userId={}, roomId={}, action={}", 
                    userId, roomId, message.getAction());
            
        } catch (Exception e) {
            log.error("处理房间音乐控制失败: roomId={}", roomId, e);
        }
    }

    /**
     * 加入音乐房间
     * 
     * @param roomId 房间ID
     * @param principal 用户身份
     * @return 加入结果消息
     */
    @MessageMapping("/music/room/{roomId}/join")
    @SendToUser("/queue/music/room")
    public WebSocketMessage joinMusicRoom(@DestinationVariable String roomId, Principal principal) {
        try {
            WebSocketAuthInterceptor.WebSocketUserPrincipal user = 
                    (WebSocketAuthInterceptor.WebSocketUserPrincipal) principal;
            
            Long userId = user.getUserId();
            
            boolean success = musicPlayService.joinMusicRoom(userId, roomId);
            
            if (success) {
                log.info("用户加入音乐房间成功: userId={}, roomId={}", userId, roomId);
                return WebSocketMessage.success("JOIN_ROOM_SUCCESS", 
                        Map.of("roomId", roomId, "message", "成功加入音乐房间"));
            } else {
                return WebSocketMessage.error("JOIN_ROOM_ERROR", "加入音乐房间失败");
            }
            
        } catch (Exception e) {
            log.error("加入音乐房间失败: roomId={}", roomId, e);
            return WebSocketMessage.error("JOIN_ROOM_ERROR", "加入音乐房间失败: " + e.getMessage());
        }
    }

    /**
     * 离开音乐房间
     * 
     * @param roomId 房间ID
     * @param principal 用户身份
     * @return 离开结果消息
     */
    @MessageMapping("/music/room/{roomId}/leave")
    @SendToUser("/queue/music/room")
    public WebSocketMessage leaveMusicRoom(@DestinationVariable String roomId, Principal principal) {
        try {
            WebSocketAuthInterceptor.WebSocketUserPrincipal user = 
                    (WebSocketAuthInterceptor.WebSocketUserPrincipal) principal;
            
            Long userId = user.getUserId();
            
            boolean success = musicPlayService.leaveMusicRoom(userId, roomId);
            
            if (success) {
                log.info("用户离开音乐房间成功: userId={}, roomId={}", userId, roomId);
                return WebSocketMessage.success("LEAVE_ROOM_SUCCESS", 
                        Map.of("roomId", roomId, "message", "成功离开音乐房间"));
            } else {
                return WebSocketMessage.error("LEAVE_ROOM_ERROR", "离开音乐房间失败");
            }
            
        } catch (Exception e) {
            log.error("离开音乐房间失败: roomId={}", roomId, e);
            return WebSocketMessage.error("LEAVE_ROOM_ERROR", "离开音乐房间失败: " + e.getMessage());
        }
    }

    /**
     * 处理聊天消息
     * 
     * @param message 聊天消息
     * @param principal 用户身份
     */
    @MessageMapping("/chat/send")
    public void handleChatMessage(@Payload ChatMessage message, Principal principal) {
        try {
            WebSocketAuthInterceptor.WebSocketUserPrincipal user = 
                    (WebSocketAuthInterceptor.WebSocketUserPrincipal) principal;
            
            Long userId = user.getUserId();
            String username = user.getUsername();
            
            // 设置发送者信息
            message.setSenderId(userId);
            message.setSenderName(username);
            message.setSendTime(LocalDateTime.now());
            message.setStatus("SENT");
            
            // 生成消息ID
            message.setMessageId("msg_" + System.currentTimeMillis() + "_" + userId);
            
            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type("CHAT_MESSAGE")
                    .content(message)
                    .senderId(userId)
                    .senderName(username)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // 如果是私聊消息
            if (message.getReceiverId() != null) {
                // 发送给接收者
                messagingTemplate.convertAndSendToUser(
                        message.getReceiverId().toString(),
                        "/queue/chat/private",
                        wsMessage
                );
                
                // 发送给发送者（确认消息）
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/chat/private",
                        wsMessage
                );
                
                log.debug("发送私聊消息: senderId={}, receiverId={}", userId, message.getReceiverId());
            }
            // 如果是群聊消息
            else if (message.getChatRoomId() != null) {
                messagingTemplate.convertAndSend(
                        "/topic/chat/room/" + message.getChatRoomId(),
                        wsMessage
                );
                
                log.debug("发送群聊消息: senderId={}, roomId={}", userId, message.getChatRoomId());
            }
            
            // 更新用户活跃时间
            onlineUserService.updateUserActivity(userId);
            
        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
        }
    }

    /**
     * 处理用户状态更新
     * 
     * @param statusMessage 状态消息
     * @param principal 用户身份
     * @return 更新结果消息
     */
    @MessageMapping("/user/status")
    @SendToUser("/queue/user/status")
    public WebSocketMessage updateUserStatus(@Payload UserOnlineStatusMessage statusMessage, 
                                           Principal principal) {
        try {
            WebSocketAuthInterceptor.WebSocketUserPrincipal user = 
                    (WebSocketAuthInterceptor.WebSocketUserPrincipal) principal;
            
            Long userId = user.getUserId();
            
            // 更新用户状态
            onlineUserService.updateUserStatus(userId, statusMessage.getStatus());
            
            log.debug("更新用户状态: userId={}, status={}", userId, statusMessage.getStatus());
            
            return WebSocketMessage.success("STATUS_UPDATE_SUCCESS", "状态更新成功");
            
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return WebSocketMessage.error("STATUS_UPDATE_ERROR", "状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取在线用户列表
     * 
     * @param principal 用户身份
     * @return 在线用户列表
     */
    @MessageMapping("/user/online/list")
    @SendToUser("/queue/user/online")
    public WebSocketMessage getOnlineUsers(Principal principal) {
        try {
            WebSocketAuthInterceptor.WebSocketUserPrincipal user = 
                    (WebSocketAuthInterceptor.WebSocketUserPrincipal) principal;
            
            Long userId = user.getUserId();
            
            // 获取在线用户列表
            onlineUserService.sendOnlineUsersToUser(userId);
            
            log.debug("获取在线用户列表: userId={}", userId);
            
            return WebSocketMessage.success("GET_ONLINE_USERS_SUCCESS", "获取在线用户列表成功");
            
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
            return WebSocketMessage.error("GET_ONLINE_USERS_ERROR", "获取在线用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 处理心跳消息
     * 
     * @param principal 用户身份
     * @return 心跳响应
     */
    @MessageMapping("/heartbeat")
    @SendToUser("/queue/heartbeat")
    public WebSocketMessage handleHeartbeat(Principal principal) {
        try {
            WebSocketAuthInterceptor.WebSocketUserPrincipal user = 
                    (WebSocketAuthInterceptor.WebSocketUserPrincipal) principal;
            
            Long userId = user.getUserId();
            
            // 更新用户活跃时间
            onlineUserService.updateUserActivity(userId);
            
            return WebSocketMessage.builder()
                    .type("HEARTBEAT_RESPONSE")
                    .content("pong")
                    .timestamp(LocalDateTime.now())
                    .build();
            
        } catch (Exception e) {
            log.error("处理心跳消息失败", e);
            return WebSocketMessage.error("HEARTBEAT_ERROR", "心跳处理失败");
        }
    }

    /**
     * 获取用户播放状态
     * 
     * @param principal 用户身份
     * @return 播放状态
     */
    @MessageMapping("/music/status/get")
    @SendToUser("/queue/music/status")
    public WebSocketMessage getUserPlayStatus(Principal principal) {
        try {
            WebSocketAuthInterceptor.WebSocketUserPrincipal user = 
                    (WebSocketAuthInterceptor.WebSocketUserPrincipal) principal;
            
            Long userId = user.getUserId();
            
            MusicPlayStatusMessage status = musicPlayService.getUserPlayStatus(userId);
            
            return WebSocketMessage.builder()
                    .type("USER_PLAY_STATUS")
                    .content(status)
                    .timestamp(LocalDateTime.now())
                    .build();
            
        } catch (Exception e) {
            log.error("获取用户播放状态失败", e);
            return WebSocketMessage.error("GET_PLAY_STATUS_ERROR", "获取播放状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取房间播放状态
     * 
     * @param roomId 房间ID
     * @param principal 用户身份
     * @return 房间播放状态
     */
    @MessageMapping("/music/room/{roomId}/status")
    @SendToUser("/queue/music/room")
    public WebSocketMessage getRoomPlayStatus(@DestinationVariable String roomId, Principal principal) {
        try {
            MusicPlayStatusMessage status = musicPlayService.getRoomPlayStatus(roomId);
            
            return WebSocketMessage.builder()
                    .type("ROOM_PLAY_STATUS")
                    .content(status)
                    .timestamp(LocalDateTime.now())
                    .build();
            
        } catch (Exception e) {
            log.error("获取房间播放状态失败: roomId={}", roomId, e);
            return WebSocketMessage.error("GET_ROOM_STATUS_ERROR", "获取房间播放状态失败: " + e.getMessage());
        }
    }

    /**
     * 广播系统通知
     * 
     * @param notification 系统通知
     */
    @MessageMapping("/system/notification")
    @SendTo("/topic/system/notifications")
    public WebSocketMessage broadcastSystemNotification(@Payload SystemNotificationMessage notification) {
        try {
            notification.setNotificationTime(LocalDateTime.now());
            
            log.info("广播系统通知: type={}, title={}", 
                    notification.getNotificationType(), notification.getTitle());
            
            return WebSocketMessage.builder()
                    .type("SYSTEM_NOTIFICATION")
                    .content(notification)
                    .timestamp(LocalDateTime.now())
                    .build();
            
        } catch (Exception e) {
            log.error("广播系统通知失败", e);
            return WebSocketMessage.error("NOTIFICATION_ERROR", "系统通知失败: " + e.getMessage());
        }
    }
}
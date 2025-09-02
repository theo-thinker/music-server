package com.musicserver.controller;

import com.musicserver.common.Result;
import com.musicserver.dto.websocket.*;
import com.musicserver.security.CurrentUser;
import com.musicserver.websocket.service.MusicPlayService;
import com.musicserver.websocket.service.OnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * WebSocket REST API控制器
 * <p>
 * 提供WebSocket相关的REST接口
 * 包括在线状态查询、播放状态获取、系统通知发送等
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
@Tag(name = "WebSocket管理", description = "WebSocket相关功能管理接口")
public class WebSocketController {

    private final OnlineUserService onlineUserService;
    private final MusicPlayService musicPlayService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    @GetMapping("/online/users")
    @Operation(summary = "获取在线用户列表", description = "获取当前所有在线用户的状态信息")
    public Result<List<UserOnlineStatusMessage>> getOnlineUsers() {
        try {
            List<UserOnlineStatusMessage> onlineUsers = onlineUserService.getOnlineUsers();

            return Result.success(onlineUsers, "获取在线用户列表成功");
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
            return Result.error("获取在线用户列表失败");
        }
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    @GetMapping("/online/count")
    @Operation(summary = "获取在线用户数量", description = "获取当前在线用户的总数量")
    public Result<Integer> getOnlineUserCount() {
        try {
            int count = onlineUserService.getOnlineUserCount();

            return Result.success(count, "获取在线用户数量成功");
        } catch (Exception e) {
            log.error("获取在线用户数量失败", e);
            return Result.error("获取在线用户数量失败");
        }
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    @GetMapping("/online/check/{userId}")
    @Operation(summary = "检查用户是否在线", description = "检查指定用户是否在线")
    public Result<Boolean> checkUserOnline(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        try {
            boolean isOnline = onlineUserService.isUserOnline(userId);

            return Result.success(isOnline, "检查用户在线状态成功");
        } catch (Exception e) {
            log.error("检查用户在线状态失败: userId={}", userId, e);
            return Result.error("检查用户在线状态失败");
        }
    }

    /**
     * 获取用户播放状态
     *
     * @param currentUser 当前用户
     * @return 播放状态
     */
    @GetMapping("/music/status")
    @Operation(summary = "获取用户播放状态", description = "获取当前用户的音乐播放状态")
    public Result<MusicPlayStatusMessage> getUserPlayStatus(@CurrentUser Long currentUser) {
        try {
            MusicPlayStatusMessage status = musicPlayService.getUserPlayStatus(currentUser);

            return Result.success(status, "获取播放状态成功");
        } catch (Exception e) {
            log.error("获取用户播放状态失败: userId={}", currentUser, e);
            return Result.error("获取播放状态失败");
        }
    }

    /**
     * 获取房间播放状态
     *
     * @param roomId 房间ID
     * @return 房间播放状态
     */
    @GetMapping("/music/room/{roomId}/status")
    @Operation(summary = "获取房间播放状态", description = "获取指定房间的音乐播放状态")
    public Result<MusicPlayStatusMessage> getRoomPlayStatus(
            @Parameter(description = "房间ID") @PathVariable String roomId) {
        try {
            MusicPlayStatusMessage status = musicPlayService.getRoomPlayStatus(roomId);

            return Result.success(status, "获取房间播放状态成功");
        } catch (Exception e) {
            log.error("获取房间播放状态失败: roomId={}", roomId, e);
            return Result.error("获取房间播放状态失败");
        }
    }

    /**
     * 发送系统通知
     *
     * @param notification 系统通知消息
     * @return 发送结果
     */
    @PostMapping("/notification/system")
    @Operation(summary = "发送系统通知", description = "向所有在线用户发送系统通知")
    public Result<String> sendSystemNotification(@RequestBody SystemNotificationMessage notification) {
        try {
            notification.setNotificationTime(LocalDateTime.now());
            notification.setNotificationId("sys_" + System.currentTimeMillis());

            WebSocketMessage message = WebSocketMessage.builder()
                    .type("SYSTEM_NOTIFICATION")
                    .content(notification)
                    .timestamp(LocalDateTime.now())
                    .build();

            // 广播给所有在线用户
            messagingTemplate.convertAndSend("/topic/system/notifications", message);

            log.info("发送系统通知成功: type={}, title={}",
                    notification.getNotificationType(), notification.getTitle());

            return Result.success("发送系统通知成功");
        } catch (Exception e) {
            log.error("发送系统通知失败", e);
            return Result.error("发送系统通知失败");
        }
    }

    /**
     * 向指定用户发送通知
     *
     * @param userId       用户ID
     * @param notification 通知消息
     * @return 发送结果
     */
    @PostMapping("/notification/user/{userId}")
    @Operation(summary = "向指定用户发送通知", description = "向指定用户发送个人通知")
    public Result<String> sendUserNotification(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestBody SystemNotificationMessage notification) {
        try {
            notification.setNotificationTime(LocalDateTime.now());
            notification.setNotificationId("user_" + System.currentTimeMillis());
            notification.setTargetUserId(userId);

            WebSocketMessage message = WebSocketMessage.builder()
                    .type("USER_NOTIFICATION")
                    .content(notification)
                    .timestamp(LocalDateTime.now())
                    .build();

            // 发送给指定用户
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    message
            );

            log.info("发送用户通知成功: userId={}, type={}",
                    userId, notification.getNotificationType());

            return Result.success("发送用户通知成功");
        } catch (Exception e) {
            log.error("发送用户通知失败: userId={}", userId, e);
            return Result.error("发送用户通知失败");
        }
    }

    /**
     * 强制用户下线
     *
     * @param userId 用户ID
     * @param reason 下线原因
     * @return 操作结果
     */
    @PostMapping("/user/{userId}/kick")
    @Operation(summary = "强制用户下线", description = "强制指定用户下线")
    public Result<String> kickUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "下线原因") @RequestParam(required = false) String reason) {
        try {
            OnlineUserService.UserSession session = onlineUserService.getUserSession(userId);
            if (session == null) {
                return Result.error("用户不在线");
            }

            // 发送强制下线通知
            SystemNotificationMessage notification = SystemNotificationMessage.builder()
                    .notificationType("FORCE_LOGOUT")
                    .title("账号异常")
                    .content(reason != null ? reason : "您的账号在其他地方登录，已被强制下线")
                    .level("WARNING")
                    .targetUserId(userId)
                    .notificationTime(LocalDateTime.now())
                    .build();

            WebSocketMessage message = WebSocketMessage.builder()
                    .type("FORCE_LOGOUT")
                    .content(notification)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/system/force-logout",
                    message
            );

            // 移除用户
            onlineUserService.removeOnlineUser(userId, session.getSessionId());

            log.info("强制用户下线成功: userId={}, reason={}", userId, reason);

            return Result.success("强制用户下线成功");
        } catch (Exception e) {
            log.error("强制用户下线失败: userId={}", userId, e);
            return Result.error("强制用户下线失败");
        }
    }

    /**
     * 广播音乐推荐
     *
     * @param musicId 音乐ID
     * @param title   音乐标题
     * @param artist  艺术家
     * @return 广播结果
     */
    @PostMapping("/broadcast/music-recommend")
    @Operation(summary = "广播音乐推荐", description = "向所有在线用户广播音乐推荐")
    public Result<String> broadcastMusicRecommend(
            @Parameter(description = "音乐ID") @RequestParam Long musicId,
            @Parameter(description = "音乐标题") @RequestParam String title,
            @Parameter(description = "艺术家") @RequestParam String artist) {
        try {
            SystemNotificationMessage notification = SystemNotificationMessage.builder()
                    .notificationType("NEW_MUSIC")
                    .title("音乐推荐")
                    .content(String.format("为您推荐音乐：%s - %s", title, artist))
                    .level("INFO")
                    .actionType("PLAY")
                    .actionText("立即播放")
                    .actionUrl("/music/" + musicId)
                    .relatedEntityId(musicId)
                    .relatedEntityType("MUSIC")
                    .notificationTime(LocalDateTime.now())
                    .build();

            WebSocketMessage message = WebSocketMessage.builder()
                    .type("MUSIC_RECOMMEND")
                    .content(notification)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/music/recommend", message);

            log.info("广播音乐推荐成功: musicId={}, title={}", musicId, title);

            return Result.success("广播音乐推荐成功");
        } catch (Exception e) {
            log.error("广播音乐推荐失败: musicId={}", musicId, e);
            return Result.error("广播音乐推荐失败");
        }
    }

    /**
     * 获取WebSocket连接统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取WebSocket统计信息", description = "获取WebSocket连接和使用统计信息")
    public Result<Map<String, Object>> getWebSocketStats() {
        try {
            Map<String, Object> stats = Map.of(
                    "onlineUserCount", onlineUserService.getOnlineUserCount(),
                    "timestamp", LocalDateTime.now(),
                    "serverStatus", "RUNNING"
            );

            return Result.success(stats, "获取统计信息成功");
        } catch (Exception e) {
            log.error("获取WebSocket统计信息失败", e);
            return Result.error("获取统计信息失败");
        }
    }

    /**
     * 测试WebSocket连接
     *
     * @return 测试结果
     */
    @PostMapping("/test/ping")
    @Operation(summary = "测试WebSocket连接", description = "发送测试消息检查WebSocket连接状态")
    public Result<String> testWebSocketConnection() {
        try {
            WebSocketMessage testMessage = WebSocketMessage.builder()
                    .type("PING_TEST")
                    .content("WebSocket连接测试消息")
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/test/ping", testMessage);

            return Result.success("WebSocket连接测试消息发送成功");
        } catch (Exception e) {
            log.error("WebSocket连接测试失败", e);
            return Result.error("WebSocket连接测试失败");
        }
    }
}
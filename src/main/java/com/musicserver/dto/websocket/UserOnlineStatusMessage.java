package com.musicserver.dto.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户在线状态消息DTO
 * 
 * 用于WebSocket传输用户在线状态信息
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户在线状态消息DTO")
public class UserOnlineStatusMessage {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "管理员")
    private String nickname;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "http://localhost:9000/image-files/avatar.jpg")
    private String avatarUrl;

    /**
     * 在线状态
     * ONLINE: 在线
     * OFFLINE: 离线
     * AWAY: 离开
     * BUSY: 忙碌
     */
    @Schema(description = "在线状态", example = "ONLINE")
    private String status;

    /**
     * 状态变化时间
     */
    @Schema(description = "状态变化时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime statusTime;

    /**
     * 最后活跃时间
     */
    @Schema(description = "最后活跃时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveTime;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型", example = "WEB")
    private String deviceType;

    /**
     * IP地址
     */
    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    /**
     * 位置信息
     */
    @Schema(description = "位置信息", example = "北京市")
    private String location;

    /**
     * 当前正在播放的音乐ID
     */
    @Schema(description = "当前正在播放的音乐ID", example = "1")
    private Long currentMusicId;

    /**
     * 当前正在播放的音乐标题
     */
    @Schema(description = "当前正在播放的音乐标题", example = "告白气球")
    private String currentMusicTitle;

    /**
     * 是否正在播放音乐
     */
    @Schema(description = "是否正在播放音乐", example = "true")
    private Boolean isPlaying;

    /**
     * 所在房间ID
     */
    @Schema(description = "所在房间ID", example = "room_001")
    private String roomId;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "session_123")
    private String sessionId;
}
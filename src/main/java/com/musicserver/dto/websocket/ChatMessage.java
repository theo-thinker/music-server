package com.musicserver.dto.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息DTO
 * <p>
 * 用于WebSocket传输聊天消息
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息DTO")
public class ChatMessage {

    /**
     * 消息ID
     */
    @Schema(description = "消息ID", example = "msg_123456")
    private String messageId;

    /**
     * 发送者ID
     */
    @Schema(description = "发送者ID", example = "1")
    private Long senderId;

    /**
     * 发送者用户名
     */
    @Schema(description = "发送者用户名", example = "admin")
    private String senderName;

    /**
     * 发送者昵称
     */
    @Schema(description = "发送者昵称", example = "管理员")
    private String senderNickname;

    /**
     * 发送者头像URL
     */
    @Schema(description = "发送者头像URL", example = "http://localhost:9000/image-files/avatar.jpg")
    private String senderAvatarUrl;

    /**
     * 接收者ID（私聊消息）
     */
    @Schema(description = "接收者ID", example = "2")
    private Long receiverId;

    /**
     * 接收者用户名（私聊消息）
     */
    @Schema(description = "接收者用户名", example = "user")
    private String receiverName;

    /**
     * 聊天室ID（群聊消息）
     */
    @Schema(description = "聊天室ID", example = "room_001")
    private String chatRoomId;

    /**
     * 聊天室名称
     */
    @Schema(description = "聊天室名称", example = "音乐爱好者群")
    private String chatRoomName;

    /**
     * 消息类型
     * TEXT: 文本消息
     * IMAGE: 图片消息
     * AUDIO: 音频消息
     * FILE: 文件消息
     * MUSIC: 音乐分享
     * SYSTEM: 系统消息
     */
    @Schema(description = "消息类型", example = "TEXT")
    private String messageType;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容", example = "这首歌真好听！")
    private String content;

    /**
     * 媒体文件URL（图片、音频、文件等）
     */
    @Schema(description = "媒体文件URL", example = "http://localhost:9000/image-files/image.jpg")
    private String mediaUrl;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小", example = "1024")
    private Long fileSize;

    /**
     * 文件名
     */
    @Schema(description = "文件名", example = "image.jpg")
    private String fileName;

    /**
     * 消息发送时间
     */
    @Schema(description = "消息发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /**
     * 消息状态
     * SENDING: 发送中
     * SENT: 已发送
     * DELIVERED: 已送达
     * READ: 已读
     * FAILED: 发送失败
     */
    @Schema(description = "消息状态", example = "SENT")
    private String status;

    /**
     * 回复的消息ID
     */
    @Schema(description = "回复的消息ID", example = "msg_123455")
    private String replyToMessageId;

    /**
     * 回复的消息内容
     */
    @Schema(description = "回复的消息内容", example = "原消息内容")
    private String replyToContent;

    /**
     * 是否置顶消息
     */
    @Schema(description = "是否置顶消息", example = "false")
    private Boolean pinned;

    /**
     * 提及的用户ID列表（@功能）
     */
    @Schema(description = "提及的用户ID列表")
    private java.util.List<Long> mentionedUserIds;

    /**
     * 分享的音乐ID
     */
    @Schema(description = "分享的音乐ID", example = "1")
    private Long sharedMusicId;

    /**
     * 分享的音乐标题
     */
    @Schema(description = "分享的音乐标题", example = "告白气球")
    private String sharedMusicTitle;

    /**
     * 分享的音乐艺术家
     */
    @Schema(description = "分享的音乐艺术家", example = "周杰伦")
    private String sharedMusicArtist;
}
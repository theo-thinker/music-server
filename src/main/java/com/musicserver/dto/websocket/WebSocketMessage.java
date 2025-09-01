package com.musicserver.dto.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket消息基础DTO
 * 
 * WebSocket消息的基础结构
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "WebSocket消息基础DTO")
public class WebSocketMessage {

    /**
     * 消息类型
     */
    @Schema(description = "消息类型", example = "MUSIC_PLAY")
    private String type;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private Object content;

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
     * 接收者ID（私聊消息）
     */
    @Schema(description = "接收者ID", example = "2")
    private Long receiverId;

    /**
     * 房间ID（房间消息）
     */
    @Schema(description = "房间ID", example = "room_001")
    private String roomId;

    /**
     * 消息时间戳
     */
    @Schema(description = "消息时间戳")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extra;

    /**
     * 消息ID
     */
    @Schema(description = "消息ID", example = "msg_123456")
    private String messageId;

    /**
     * 消息状态
     */
    @Schema(description = "消息状态", example = "SENT")
    private String status;

    public static WebSocketMessage success(String type, Object content) {
        return WebSocketMessage.builder()
                .type(type)
                .content(content)
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .build();
    }

    public static WebSocketMessage error(String type, String errorMessage) {
        return WebSocketMessage.builder()
                .type(type)
                .content(errorMessage)
                .timestamp(LocalDateTime.now())
                .status("ERROR")
                .build();
    }
}
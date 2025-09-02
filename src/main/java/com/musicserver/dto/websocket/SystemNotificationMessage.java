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
 * 系统通知消息DTO
 * <p>
 * 用于WebSocket传输系统通知信息
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统通知消息DTO")
public class SystemNotificationMessage {

    /**
     * 通知ID
     */
    @Schema(description = "通知ID", example = "notif_123456")
    private String notificationId;

    /**
     * 通知类型
     * SYSTEM_MAINTENANCE: 系统维护
     * NEW_MUSIC: 新音乐上线
     * FRIEND_REQUEST: 好友请求
     * COMMENT: 评论通知
     * LIKE: 点赞通知
     * FOLLOW: 关注通知
     * PLAYLIST_SHARE: 播放列表分享
     * SYSTEM_UPDATE: 系统更新
     * ANNOUNCEMENT: 公告
     */
    @Schema(description = "通知类型", example = "NEW_MUSIC")
    private String notificationType;

    /**
     * 通知标题
     */
    @Schema(description = "通知标题", example = "新音乐上线")
    private String title;

    /**
     * 通知内容
     */
    @Schema(description = "通知内容", example = "周杰伦的新歌《告白气球》已上线，快来收听吧！")
    private String content;

    /**
     * 通知级别
     * INFO: 信息
     * WARNING: 警告
     * ERROR: 错误
     * SUCCESS: 成功
     */
    @Schema(description = "通知级别", example = "INFO")
    private String level;

    /**
     * 目标用户ID（null表示广播给所有用户）
     */
    @Schema(description = "目标用户ID", example = "1")
    private Long targetUserId;

    /**
     * 发送者ID（系统通知可能为null）
     */
    @Schema(description = "发送者ID", example = "0")
    private Long senderId;

    /**
     * 发送者名称
     */
    @Schema(description = "发送者名称", example = "系统")
    private String senderName;

    /**
     * 通知时间
     */
    @Schema(description = "通知时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime notificationTime;

    /**
     * 是否已读
     */
    @Schema(description = "是否已读", example = "false")
    private Boolean isRead;

    /**
     * 跳转链接
     */
    @Schema(description = "跳转链接", example = "/music/1")
    private String actionUrl;

    /**
     * 操作类型
     * VIEW: 查看
     * DOWNLOAD: 下载
     * PLAY: 播放
     * SHARE: 分享
     * IGNORE: 忽略
     */
    @Schema(description = "操作类型", example = "PLAY")
    private String actionType;

    /**
     * 操作文本
     */
    @Schema(description = "操作文本", example = "立即播放")
    private String actionText;

    /**
     * 图标URL
     */
    @Schema(description = "图标URL", example = "http://localhost:9000/image-files/notification-icon.png")
    private String iconUrl;

    /**
     * 缩略图URL
     */
    @Schema(description = "缩略图URL", example = "http://localhost:9000/image-files/thumbnail.jpg")
    private String thumbnailUrl;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 是否持久化
     */
    @Schema(description = "是否持久化", example = "true")
    private Boolean persistent;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;

    /**
     * 关联的实体ID（如音乐ID、播放列表ID等）
     */
    @Schema(description = "关联的实体ID", example = "1")
    private Long relatedEntityId;

    /**
     * 关联的实体类型
     */
    @Schema(description = "关联的实体类型", example = "MUSIC")
    private String relatedEntityType;

    /**
     * 通知分组
     */
    @Schema(description = "通知分组", example = "MUSIC_UPDATES")
    private String group;

    /**
     * 优先级（1-10，数字越大优先级越高）
     */
    @Schema(description = "优先级", example = "5")
    private Integer priority;
}
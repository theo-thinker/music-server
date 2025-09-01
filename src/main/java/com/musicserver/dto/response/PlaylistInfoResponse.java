package com.musicserver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 播放列表信息响应DTO
 * 
 * 用于返回播放列表详细信息
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Schema(description = "播放列表信息响应")
public class PlaylistInfoResponse {

    /**
     * 播放列表ID
     */
    @Schema(description = "播放列表ID", example = "1001")
    private Long id;

    /**
     * 播放列表名称
     */
    @Schema(description = "播放列表名称", example = "我的收藏")
    private String name;

    /**
     * 播放列表描述
     */
    @Schema(description = "播放列表描述", example = "这是我最喜欢的歌曲合集")
    private String description;

    /**
     * 播放列表封面URL
     */
    @Schema(description = "播放列表封面URL", example = "/static/covers/playlist1.jpg")
    private String cover;

    /**
     * 创建者用户ID
     */
    @Schema(description = "创建者用户ID", example = "1")
    private Long userId;

    /**
     * 创建者用户名
     */
    @Schema(description = "创建者用户名", example = "musiclover")
    private String username;

    /**
     * 创建者昵称
     */
    @Schema(description = "创建者昵称", example = "音乐爱好者")
    private String nickname;

    /**
     * 是否公开：0-私有，1-公开
     */
    @Schema(description = "是否公开", example = "1", allowableValues = {"0", "1"})
    private Integer isPublic;

    /**
     * 包含歌曲数量
     */
    @Schema(description = "歌曲数量", example = "25")
    private Integer musicCount;

    /**
     * 播放次数
     */
    @Schema(description = "播放次数", example = "1280")
    private Long playCount;

    /**
     * 收藏次数
     */
    @Schema(description = "收藏次数", example = "88")
    private Long collectCount;

    /**
     * 状态：0-删除，1-正常
     */
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-09-01T10:00:00")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-09-01T14:30:00")
    private LocalDateTime updatedTime;

    /**
     * 是否已收藏（当前用户）
     */
    @Schema(description = "是否已收藏", example = "false")
    private Boolean isCollected = false;

    /**
     * 是否为当前用户创建
     */
    @Schema(description = "是否为当前用户创建", example = "true")
    private Boolean isOwner = false;
}
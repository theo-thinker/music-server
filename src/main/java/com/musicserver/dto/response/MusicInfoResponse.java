package com.musicserver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 音乐信息响应DTO
 * <p>
 * 用于返回音乐详细信息
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Schema(description = "音乐信息响应")
public class MusicInfoResponse {

    /**
     * 音乐ID
     */
    @Schema(description = "音乐ID", example = "100")
    private Long id;

    /**
     * 歌曲名称
     */
    @Schema(description = "歌曲名称", example = "青花瓷")
    private String title;

    /**
     * 艺术家ID
     */
    @Schema(description = "艺术家ID", example = "1")
    private Long artistId;

    /**
     * 艺术家名称
     */
    @Schema(description = "艺术家名称", example = "周杰伦")
    private String artistName;

    /**
     * 专辑ID
     */
    @Schema(description = "专辑ID", example = "10")
    private Long albumId;

    /**
     * 专辑名称
     */
    @Schema(description = "专辑名称", example = "我很忙")
    private String albumName;

    /**
     * 专辑封面URL
     */
    @Schema(description = "专辑封面URL", example = "/static/covers/album1.jpg")
    private String albumCover;

    /**
     * 歌曲时长（秒）
     */
    @Schema(description = "歌曲时长(秒)", example = "248")
    private Integer duration;

    /**
     * 音频文件URL
     */
    @Schema(description = "音频文件URL", example = "/static/music/song1.mp3")
    private String fileUrl;

    /**
     * 歌词文件URL
     */
    @Schema(description = "歌词文件URL", example = "/static/lyrics/song1.lrc")
    private String lrcUrl;

    /**
     * 音质等级：1-标准，2-高品质，3-无损
     */
    @Schema(description = "音质等级", example = "2", allowableValues = {"1", "2", "3"})
    private Integer quality;

    /**
     * 音频文件大小（字节）
     */
    @Schema(description = "文件大小(字节)", example = "5242880")
    private Long fileSize;

    /**
     * 音频格式
     */
    @Schema(description = "音频格式", example = "mp3")
    private String format;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "5")
    private Long categoryId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "流行")
    private String categoryName;

    /**
     * 播放次数
     */
    @Schema(description = "播放次数", example = "128456")
    private Long playCount;

    /**
     * 点赞次数
     */
    @Schema(description = "点赞次数", example = "5280")
    private Long likeCount;

    /**
     * 收藏次数
     */
    @Schema(description = "收藏次数", example = "2880")
    private Long collectCount;

    /**
     * 状态：0-下架，1-正常
     */
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间", example = "2025-09-01T14:30:00")
    private LocalDateTime releaseDate;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-09-01T10:00:00")
    private LocalDateTime createdTime;

    /**
     * 是否已收藏（当前用户）
     */
    @Schema(description = "是否已收藏", example = "true")
    private Boolean isCollected = false;

    /**
     * 是否已点赞（当前用户）
     */
    @Schema(description = "是否已点赞", example = "false")
    private Boolean isLiked = false;
}
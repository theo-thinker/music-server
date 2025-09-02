package com.musicserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.musicserver.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 音乐实体类
 * <p>
 * 对应数据库music表，存储音乐的详细信息
 * 包括基本信息、文件信息、统计数据等
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("music")
@Schema(description = "音乐信息实体")
public class Music extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌曲名称
     */
    @TableField("title")
    @Schema(description = "歌曲名称", example = "可爱女人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 艺术家ID，外键
     */
    @TableField("artist_id")
    @Schema(description = "艺术家ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long artistId;

    /**
     * 专辑ID，外键，可为空（单曲）
     */
    @TableField("album_id")
    @Schema(description = "专辑ID", example = "1")
    private Long albumId;

    /**
     * 专辑封面URL
     */
    @TableField("album_cover")
    @Schema(description = "专辑封面", example = "/static/albums/jay_cover.jpg")
    private String albumCover;

    /**
     * 歌曲时长（秒）
     */
    @TableField("duration")
    @Schema(description = "歌曲时长(秒)", example = "221", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;

    /**
     * 音频文件URL地址
     */
    @TableField("file_url")
    @Schema(description = "音频文件URL", example = "/static/music/keai_nvren.mp3", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;

    /**
     * 歌词文件URL地址
     */
    @TableField("lrc_url")
    @Schema(description = "歌词文件URL", example = "/static/lyrics/keai_nvren.lrc")
    private String lrcUrl;

    /**
     * 音质等级：1-标准，2-高品质，3-无损
     */
    @TableField("quality")
    @Schema(description = "音质等级", example = "1", allowableValues = {"1", "2", "3"})
    private Integer quality;

    /**
     * 音频文件大小（字节）
     */
    @TableField("file_size")
    @Schema(description = "文件大小(字节)", example = "5242880")
    private Long fileSize;

    /**
     * 音频格式：mp3、flac、wav等
     */
    @TableField("format")
    @Schema(description = "音频格式", example = "mp3")
    private String format;

    /**
     * 音乐分类ID，外键
     */
    @TableField("category_id")
    @Schema(description = "音乐分类ID", example = "1")
    private Long categoryId;

    /**
     * 播放次数
     */
    @TableField("play_count")
    @Schema(description = "播放次数", example = "1280000")
    private Long playCount;

    /**
     * 点赞次数
     */
    @TableField("like_count")
    @Schema(description = "点赞次数", example = "52800")
    private Long likeCount;

    /**
     * 收藏次数
     */
    @TableField("collect_count")
    @Schema(description = "收藏次数", example = "128000")
    private Long collectCount;

    /**
     * 状态：0-下架，1-正常
     */
    @TableField("status")
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    /**
     * 发布时间
     */
    @TableField("release_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "发布时间", example = "2000-11-07 00:00:00")
    private LocalDateTime releaseDate;

    // ========================================
    // 关联对象（非数据库字段）
    // ========================================

    /**
     * 关联的艺术家信息
     */
    @TableField(exist = false)
    @Schema(description = "艺术家信息")
    private Artist artist;

    /**
     * 关联的专辑信息
     */
    @TableField(exist = false)
    @Schema(description = "专辑信息")
    private Album album;

    /**
     * 关联的分类信息
     */
    @TableField(exist = false)
    @Schema(description = "分类信息")
    private MusicCategory category;

    /**
     * 音乐标签列表
     */
    @TableField(exist = false)
    @Schema(description = "标签列表")
    private List<MusicTag> tags;

    // ========================================
    // 业务字段（非数据库字段）
    // ========================================

    /**
     * 是否已收藏（针对当前用户）
     */
    @TableField(exist = false)
    @Schema(description = "是否已收藏", example = "true")
    private Boolean isCollected;

    /**
     * 是否已点赞（针对当前用户）
     */
    @TableField(exist = false)
    @Schema(description = "是否已点赞", example = "false")
    private Boolean isLiked;

    /**
     * 格式化的时长（如：03:41）
     */
    @Schema(description = "格式化时长", example = "03:41")
    public String getFormattedDuration() {
        if (duration == null || duration <= 0) {
            return "00:00";
        }
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 格式化的文件大小（如：5.2 MB）
     */
    @Schema(description = "格式化文件大小", example = "5.2 MB")
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize <= 0) {
            return "0 B";
        }

        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }
}
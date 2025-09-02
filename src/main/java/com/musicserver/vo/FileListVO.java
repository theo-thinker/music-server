package com.musicserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件列表视图对象
 * <p>
 * 用于展示文件列表的简要信息
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件列表视图")
public class FileListVO {

    /**
     * 文件ID
     */
    @Schema(description = "文件唯一标识", example = "1234567890abcdef")
    private String fileId;

    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名", example = "my-music.mp3")
    private String originalFilename;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型", example = "music")
    private String fileType;

    /**
     * MIME类型
     */
    @Schema(description = "MIME类型", example = "audio/mpeg")
    private String mimeType;

    /**
     * 文件大小（格式化）
     */
    @Schema(description = "文件大小(格式化)", example = "5.0 MB")
    private String formattedSize;

    /**
     * 文件访问URL
     */
    @Schema(description = "文件访问URL", example = "http://localhost:9000/music-files/2025/01/01/1234567890abcdef.mp3")
    private String accessUrl;

    /**
     * 缩略图URL（仅图片文件）
     */
    @Schema(description = "缩略图URL", example = "http://localhost:9000/thumbnails/thumb_1234567890abcdef.jpg")
    private String thumbnailUrl;

    /**
     * 文件描述
     */
    @Schema(description = "文件描述", example = "音乐封面图片")
    private String description;

    /**
     * 是否公开访问
     */
    @Schema(description = "是否公开访问", example = "false")
    private Boolean publicAccess;

    /**
     * 上传用户名
     */
    @Schema(description = "上传用户名", example = "admin")
    private String uploadUsername;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    /**
     * 文件状态
     */
    @Schema(description = "文件状态", example = "ACTIVE")
    private String status;
}
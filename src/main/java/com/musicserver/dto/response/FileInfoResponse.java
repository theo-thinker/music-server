package com.musicserver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息响应DTO
 * 
 * 用于返回文件基本信息
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件信息响应")
public class FileInfoResponse {

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
     * 存储文件名
     */
    @Schema(description = "存储文件名", example = "2025/01/01/1234567890abcdef.mp3")
    private String storedFilename;

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
     * 文件大小（字节）
     */
    @Schema(description = "文件大小(字节)", example = "5242880")
    private Long fileSize;

    /**
     * 文件大小（格式化）
     */
    @Schema(description = "文件大小(格式化)", example = "5.0 MB")
    private String formattedSize;

    /**
     * 存储桶名称
     */
    @Schema(description = "存储桶名称", example = "music-files")
    private String bucketName;

    /**
     * 文件访问URL
     */
    @Schema(description = "文件访问URL", example = "http://localhost:9000/music-files/2025/01/01/1234567890abcdef.mp3")
    private String accessUrl;

    /**
     * 预签名URL（临时访问链接）
     */
    @Schema(description = "预签名URL", example = "http://localhost:9000/music-files/2025/01/01/1234567890abcdef.mp3?X-Amz-...")
    private String presignedUrl;

    /**
     * 预签名URL过期时间
     */
    @Schema(description = "预签名URL过期时间")
    private LocalDateTime presignedUrlExpiry;

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
     * 业务ID
     */
    @Schema(description = "业务ID", example = "1")
    private Long businessId;

    /**
     * 上传用户ID
     */
    @Schema(description = "上传用户ID", example = "1")
    private Long uploadUserId;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    /**
     * 文件状态
     * ACTIVE：正常，DELETED：已删除，EXPIRED：已过期
     */
    @Schema(description = "文件状态", example = "ACTIVE")
    private String status;

    /**
     * ETag值（用于校验文件完整性）
     */
    @Schema(description = "ETag值", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String etag;

    /**
     * 文件MD5值
     */
    @Schema(description = "文件MD5值", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String md5;

    /**
     * 文件拓展信息（JSON格式）
     */
    @Schema(description = "文件拓展信息", example = "{\"width\": 1920, \"height\": 1080}")
    private String metadata;
}
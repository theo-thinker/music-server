package com.musicserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件详情视图对象
 * <p>
 * 用于展示文件的详细信息
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件详情视图")
public class FileDetailVO {

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
     * 上传用户信息
     */
    @Schema(description = "上传用户信息")
    private UserBasicInfo uploadUser;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    /**
     * 最后访问时间
     */
    @Schema(description = "最后访问时间")
    private LocalDateTime lastAccessTime;

    /**
     * 访问次数
     */
    @Schema(description = "访问次数", example = "100")
    private Long accessCount;

    /**
     * 文件状态
     */
    @Schema(description = "文件状态", example = "ACTIVE")
    private String status;

    /**
     * 文件标签
     */
    @Schema(description = "文件标签")
    private List<String> tags;

    /**
     * 文件拓展信息
     */
    @Schema(description = "文件拓展信息")
    private FileMetadata metadata;

    /**
     * 用户基本信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户基本信息")
    public static class UserBasicInfo {

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
         * 昵称
         */
        @Schema(description = "昵称", example = "管理员")
        private String nickname;

        /**
         * 头像URL
         */
        @Schema(description = "头像URL", example = "http://localhost:9000/avatar/avatar.jpg")
        private String avatarUrl;
    }

    /**
     * 文件元数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "文件元数据")
    public static class FileMetadata {

        /**
         * 图片宽度（仅图片文件）
         */
        @Schema(description = "图片宽度", example = "1920")
        private Integer width;

        /**
         * 图片高度（仅图片文件）
         */
        @Schema(description = "图片高度", example = "1080")
        private Integer height;

        /**
         * 音频时长（仅音频文件，单位：秒）
         */
        @Schema(description = "音频时长(秒)", example = "240")
        private Integer duration;

        /**
         * 音频比特率（仅音频文件）
         */
        @Schema(description = "音频比特率", example = "320")
        private Integer bitrate;

        /**
         * 音频采样率（仅音频文件）
         */
        @Schema(description = "音频采样率", example = "44100")
        private Integer sampleRate;

        /**
         * 艺术家（仅音频文件）
         */
        @Schema(description = "艺术家", example = "周杰伦")
        private String artist;

        /**
         * 专辑（仅音频文件）
         */
        @Schema(description = "专辑", example = "叶惠美")
        private String album;

        /**
         * 文件编码格式
         */
        @Schema(description = "文件编码格式", example = "UTF-8")
        private String encoding;

        /**
         * 创建应用程序
         */
        @Schema(description = "创建应用程序", example = "Adobe Photoshop")
        private String application;
    }
}
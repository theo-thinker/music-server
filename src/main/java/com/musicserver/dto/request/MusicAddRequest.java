package com.musicserver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 音乐添加请求DTO
 * 
 * 用于接收添加音乐时的请求参数
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Schema(description = "音乐添加请求")
public class MusicAddRequest {

    /**
     * 歌曲名称
     */
    @NotBlank(message = "歌曲名称不能为空")
    @Schema(description = "歌曲名称", example = "青花瓷", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 艺术家ID
     */
    @NotNull(message = "艺术家ID不能为空")
    @Schema(description = "艺术家ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long artistId;

    /**
     * 专辑ID（可选）
     */
    @Schema(description = "专辑ID", example = "10")
    private Long albumId;

    /**
     * 专辑封面URL
     */
    @Schema(description = "专辑封面URL", example = "/static/covers/album1.jpg")
    private String albumCover;

    /**
     * 歌曲时长（秒）
     */
    @NotNull(message = "歌曲时长不能为空")
    @Min(value = 1, message = "歌曲时长必须大于0")
    @Schema(description = "歌曲时长(秒)", example = "248", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;

    /**
     * 音频文件URL
     */
    @NotBlank(message = "音频文件URL不能为空")
    @Schema(description = "音频文件URL", example = "/static/music/song1.mp3", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;

    /**
     * 歌词文件URL
     */
    @Schema(description = "歌词文件URL", example = "/static/lyrics/song1.lrc")
    private String lrcUrl;

    /**
     * 音质等级：1-标准，2-高品质，3-无损
     */
    @Min(value = 1, message = "音质等级最小值为1")
    @Max(value = 3, message = "音质等级最大值为3")
    @Schema(description = "音质等级", example = "2", allowableValues = {"1", "2", "3"})
    private Integer quality = 1;

    /**
     * 音频文件大小（字节）
     */
    @Schema(description = "文件大小(字节)", example = "5242880")
    private Long fileSize;

    /**
     * 音频格式
     */
    @Schema(description = "音频格式", example = "mp3")
    private String format = "mp3";

    /**
     * 音乐分类ID
     */
    @Schema(description = "分类ID", example = "5")
    private Long categoryId;
}
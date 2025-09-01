package com.musicserver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 播放列表创建请求DTO
 * 
 * 用于接收创建播放列表时的请求参数
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Schema(description = "播放列表创建请求")
public class PlaylistCreateRequest {

    /**
     * 播放列表名称
     */
    @NotBlank(message = "播放列表名称不能为空")
    @Size(max = 200, message = "播放列表名称长度不能超过200个字符")
    @Schema(description = "播放列表名称", example = "我的收藏", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 播放列表描述
     */
    @Size(max = 1000, message = "播放列表描述长度不能超过1000个字符")
    @Schema(description = "播放列表描述", example = "这是我最喜欢的歌曲合集")
    private String description;

    /**
     * 播放列表封面URL
     */
    @Schema(description = "播放列表封面URL", example = "/static/covers/playlist1.jpg")
    private String cover;

    /**
     * 是否公开：0-私有，1-公开
     */
    @Schema(description = "是否公开", example = "1", allowableValues = {"0", "1"})
    private Integer isPublic = 1;
}
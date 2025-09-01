package com.musicserver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传请求DTO
 * 
 * 用于接收文件上传请求的参数
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Schema(description = "文件上传请求")
public class FileUploadRequest {

    /**
     * 上传的文件
     */
    @NotNull(message = "文件不能为空")
    @Schema(description = "上传的文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    /**
     * 文件类型
     * 可选值：music、image、lyric、avatar
     */
    @NotBlank(message = "文件类型不能为空")
    @Size(max = 20, message = "文件类型长度不能超过20个字符")
    @Schema(description = "文件类型(music/image/lyric/avatar)", requiredMode = Schema.RequiredMode.REQUIRED, example = "music")
    private String fileType;

    /**
     * 业务ID（可选）
     * 例如：音乐ID、用户ID等
     */
    @Schema(description = "业务ID", example = "1")
    private Long businessId;

    /**
     * 文件描述（可选）
     */
    @Size(max = 200, message = "文件描述长度不能超过200个字符")
    @Schema(description = "文件描述", example = "音乐封面图片")
    private String description;

    /**
     * 是否公开访问
     * true：公开访问，false：私有访问
     */
    @Schema(description = "是否公开访问", example = "false")
    private Boolean publicAccess = false;

    /**
     * 自定义文件名（可选）
     * 如果不提供，将使用系统生成的文件名
     */
    @Size(max = 100, message = "自定义文件名长度不能超过100个字符")
    @Schema(description = "自定义文件名", example = "my-music")
    private String customFilename;

    /**
     * 覆盖已存在的文件
     * true：覆盖，false：不覆盖（默认）
     */
    @Schema(description = "是否覆盖已存在的文件", example = "false")
    private Boolean overwrite = false;
}
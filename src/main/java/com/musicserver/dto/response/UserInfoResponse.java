package com.musicserver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 * 
 * 用于返回用户信息，不包含敏感数据
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Schema(description = "用户信息响应")
public class UserInfoResponse {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "musiclover")
    private String username;

    /**
     * 邮箱地址
     */
    @Schema(description = "邮箱地址", example = "user@example.com")
    private String email;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "音乐爱好者")
    private String nickname;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "/static/avatars/default.jpg")
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    private Integer gender;

    /**
     * 个性签名
     */
    @Schema(description = "个性签名", example = "音乐是灵魂的语言")
    private String signature;

    /**
     * 用户等级
     */
    @Schema(description = "用户等级", example = "5")
    private Integer level;

    /**
     * 经验值
     */
    @Schema(description = "经验值", example = "5280")
    private Long experience;

    /**
     * 用户状态：0-禁用，1-正常，2-冻结
     */
    @Schema(description = "用户状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间", example = "2025-09-01T14:30:00")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-09-01T10:00:00")
    private LocalDateTime createdTime;
}
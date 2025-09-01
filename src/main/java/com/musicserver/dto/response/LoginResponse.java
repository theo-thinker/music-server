package com.musicserver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * 
 * 用于返回登录结果，包含访问令牌和用户信息
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    /**
     * 令牌过期时间（秒）
     */
    @Schema(description = "令牌过期时间(秒)", example = "7200")
    private Long expiresIn;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfoResponse userInfo;

    public LoginResponse(String accessToken, String refreshToken, Long expiresIn, UserInfoResponse userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
    }
}
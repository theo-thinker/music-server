package com.musicserver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求DTO
 * 
 * 用于接收用户注册时的请求参数
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Schema(description = "用户名", example = "musiclover", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    /**
     * 昵称
     */
    @Size(max = 100, message = "昵称长度不能超过100个字符")
    @Schema(description = "昵称", example = "音乐爱好者")
    private String nickname;

    /**
     * 手机号码
     */
    @Size(max = 20, message = "手机号码长度不能超过20个字符")
    @Schema(description = "手机号码", example = "13800138000")
    private String phone;
}
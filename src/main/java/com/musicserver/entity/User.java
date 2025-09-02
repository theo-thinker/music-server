package com.musicserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.musicserver.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>
 * 对应数据库users表，存储用户的基本信息
 * 包括登录凭证、个人信息、等级体系等
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
@Schema(description = "用户信息实体")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名，唯一标识
     */
    @TableField("username")
    @Schema(description = "用户名", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /**
     * 用户邮箱地址
     */
    @TableField("email")
    @Schema(description = "邮箱地址", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * 用户手机号码
     */
    @TableField("phone")
    @Schema(description = "手机号码", example = "13888888888")
    private String phone;

    /**
     * 用户密码，BCrypt加密存储
     */
    @TableField("password")
    @Schema(description = "用户密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    @Schema(description = "用户昵称", example = "约翰")
    private String nickname;

    /**
     * 用户头像URL地址
     */
    @TableField("avatar")
    @Schema(description = "头像URL", example = "/static/avatars/user_123.jpg")
    private String avatar;

    /**
     * 用户性别：0-未知，1-男，2-女
     */
    @TableField("gender")
    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    private Integer gender;

    /**
     * 用户生日
     */
    @TableField("birthday")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "生日", example = "1990-01-01")
    private LocalDate birthday;

    /**
     * 用户个性签名
     */
    @TableField("signature")
    @Schema(description = "个性签名", example = "音乐是我的生命")
    private String signature;

    /**
     * 用户等级，默认为1级
     */
    @TableField("level")
    @Schema(description = "用户等级", example = "5")
    private Integer level;

    /**
     * 用户经验值
     */
    @TableField("experience")
    @Schema(description = "经验值", example = "1280")
    private Long experience;

    /**
     * 用户状态：0-禁用，1-正常，2-冻结
     */
    @TableField("status")
    @Schema(description = "用户状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "最后登录时间", example = "2025-09-01 10:30:00")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP地址
     */
    @TableField("last_login_ip")
    @Schema(description = "最后登录IP", example = "192.168.1.100")
    private String lastLoginIp;
}
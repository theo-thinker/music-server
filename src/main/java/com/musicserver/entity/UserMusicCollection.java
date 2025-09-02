package com.musicserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.musicserver.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户音乐收藏实体类
 * <p>
 * 对应数据库user_music_collections表，记录用户收藏的音乐
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_music_collections")
@Schema(description = "用户音乐收藏实体")
public class UserMusicCollection extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，外键
     */
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    /**
     * 音乐ID，外键
     */
    @TableField("music_id")
    @Schema(description = "音乐ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long musicId;

    // ========================================
    // 关联对象（非数据库字段）
    // ========================================

    /**
     * 关联的用户信息
     */
    @TableField(exist = false)
    @Schema(description = "用户信息")
    private User user;

    /**
     * 关联的音乐信息
     */
    @TableField(exist = false)
    @Schema(description = "音乐信息")
    private Music music;
}
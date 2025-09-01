package com.musicserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.musicserver.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户配置实体类
 * 
 * 对应数据库user_profiles表，存储用户的个性化配置
 * 包括音乐偏好、播放设置、隐私设置等
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_profiles")
@Schema(description = "用户配置实体")
public class UserProfile extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID，外键关联users表
     */
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    
    /**
     * 音乐偏好设置，JSON格式存储
     */
    @TableField("music_preference")
    @Schema(description = "音乐偏好设置", example = "{\"genres\":[\"pop\",\"rock\"],\"languages\":[\"cn\",\"en\"]}")
    private String musicPreference;
    
    /**
     * 自动播放设置：0-关闭，1-开启
     */
    @TableField("auto_play")
    @Schema(description = "自动播放", example = "1", allowableValues = {"0", "1"})
    private Integer autoPlay;
    
    /**
     * 音质偏好：1-标准，2-高品质，3-无损
     */
    @TableField("quality_preference")
    @Schema(description = "音质偏好", example = "2", allowableValues = {"1", "2", "3"})
    private Integer qualityPreference;
    
    /**
     * 播放模式：1-顺序播放，2-随机播放，3-单曲循环
     */
    @TableField("playback_mode")
    @Schema(description = "播放模式", example = "1", allowableValues = {"1", "2", "3"})
    private Integer playbackMode;
    
    /**
     * 默认音量大小，范围0-100
     */
    @TableField("volume")
    @Schema(description = "默认音量", example = "80", minimum = "0", maximum = "100")
    private Integer volume;
    
    /**
     * 隐私级别：1-公开，2-好友可见，3-私密
     */
    @TableField("privacy_level")
    @Schema(description = "隐私级别", example = "1", allowableValues = {"1", "2", "3"})
    private Integer privacyLevel;
}
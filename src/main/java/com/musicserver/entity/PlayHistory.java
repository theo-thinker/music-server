package com.musicserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.musicserver.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 播放历史实体类
 * <p>
 * 对应数据库play_histories表，记录用户的音乐播放历史
 * 用于统计分析和个性化推荐
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("play_histories")
@Schema(description = "播放历史实体")
public class PlayHistory extends BaseEntity {

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

    /**
     * 播放时间
     */
    @TableField("play_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "播放时间", example = "2025-09-01 14:30:00")
    private LocalDateTime playTime;

    /**
     * 实际播放时长（秒）
     */
    @TableField("play_duration")
    @Schema(description = "播放时长(秒)", example = "180")
    private Integer playDuration;

    /**
     * 设备类型
     */
    @TableField("device_type")
    @Schema(description = "设备类型", example = "mobile")
    private String deviceType;

    /**
     * 播放时的IP地址
     */
    @TableField("ip_address")
    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

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
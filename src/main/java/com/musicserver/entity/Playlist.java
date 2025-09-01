package com.musicserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.musicserver.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 播放列表实体类
 * 
 * 对应数据库playlists表，存储播放列表信息
 * 包括基本信息、统计数据和关联音乐
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("playlists")
@Schema(description = "播放列表实体")
public class Playlist extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 播放列表名称
     */
    @TableField("name")
    @Schema(description = "播放列表名称", example = "我喜欢的音乐", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    /**
     * 播放列表描述
     */
    @TableField("description")
    @Schema(description = "播放列表描述", example = "收藏的经典歌曲")
    private String description;
    
    /**
     * 播放列表封面图片URL
     */
    @TableField("cover")
    @Schema(description = "封面图片", example = "/static/playlists/my_favorite.jpg")
    private String cover;
    
    /**
     * 创建者用户ID，外键
     */
    @TableField("user_id")
    @Schema(description = "创建者ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    
    /**
     * 是否公开：0-私有，1-公开
     */
    @TableField("is_public")
    @Schema(description = "是否公开", example = "1", allowableValues = {"0", "1"})
    private Integer isPublic;
    
    /**
     * 包含歌曲数量
     */
    @TableField("music_count")
    @Schema(description = "歌曲数量", example = "25")
    private Integer musicCount;
    
    /**
     * 播放次数
     */
    @TableField("play_count")
    @Schema(description = "播放次数", example = "1580")
    private Long playCount;
    
    /**
     * 收藏次数
     */
    @TableField("collect_count")
    @Schema(description = "收藏次数", example = "128")
    private Long collectCount;
    
    /**
     * 状态：0-删除，1-正常
     */
    @TableField("status")
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;
    
    // ========================================
    // 关联对象（非数据库字段）
    // ========================================
    
    /**
     * 创建者信息
     */
    @TableField(exist = false)
    @Schema(description = "创建者信息")
    private User user;
    
    /**
     * 播放列表中的音乐列表
     */
    @TableField(exist = false)
    @Schema(description = "音乐列表")
    private List<Music> musicList;
    
    // ========================================
    // 业务字段（非数据库字段）
    // ========================================
    
    /**
     * 是否已收藏（针对当前用户）
     */
    @TableField(exist = false)
    @Schema(description = "是否已收藏", example = "true")
    private Boolean isCollected;
    
    /**
     * 是否为当前用户创建
     */
    @TableField(exist = false)
    @Schema(description = "是否为当前用户创建", example = "false")
    private Boolean isOwner;
    
    /**
     * 总时长（所有音乐时长之和，单位：秒）
     */
    @TableField(exist = false)
    @Schema(description = "总时长(秒)", example = "7200")
    private Integer totalDuration;
    
    /**
     * 格式化的总时长（如：02:00:00）
     */
    @Schema(description = "格式化总时长", example = "02:00:00")
    public String getFormattedTotalDuration() {
        if (totalDuration == null || totalDuration <= 0) {
            return "00:00:00";
        }
        int hours = totalDuration / 3600;
        int minutes = (totalDuration % 3600) / 60;
        int seconds = totalDuration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
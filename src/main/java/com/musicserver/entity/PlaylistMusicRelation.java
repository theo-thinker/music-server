package com.musicserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.musicserver.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 播放列表音乐关联实体类
 * 
 * 对应数据库playlist_music_relations表，存储播放列表与音乐的关联关系
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("playlist_music_relations")
@Schema(description = "播放列表音乐关联实体")
public class PlaylistMusicRelation extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 播放列表ID，外键
     */
    @TableField("playlist_id")
    @Schema(description = "播放列表ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long playlistId;
    
    /**
     * 音乐ID，外键
     */
    @TableField("music_id")
    @Schema(description = "音乐ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long musicId;
    
    /**
     * 歌曲在播放列表中的排序
     */
    @TableField("sort_order")
    @Schema(description = "排序位置", example = "1")
    private Integer sortOrder;
    
    /**
     * 添加时间
     */
    @TableField("added_time")
    @Schema(description = "添加时间", example = "2025-09-01T14:30:00")
    private LocalDateTime addedTime;
}
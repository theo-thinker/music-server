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

/**
 * 专辑实体类
 * 
 * 对应数据库albums表，存储专辑的基本信息
 * 包括专辑名称、封面、艺术家、发布日期等
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("albums")
@Schema(description = "专辑信息实体")
public class Album extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 专辑名称
     */
    @TableField("name")
    @Schema(description = "专辑名称", example = "Jay", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    /**
     * 专辑封面图片URL
     */
    @TableField("cover")
    @Schema(description = "专辑封面", example = "/static/albums/jay_cover.jpg")
    private String cover;
    
    /**
     * 艺术家ID，外键
     */
    @TableField("artist_id")
    @Schema(description = "艺术家ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long artistId;
    
    /**
     * 专辑描述
     */
    @TableField("description")
    @Schema(description = "专辑描述", example = "周杰伦首张个人专辑，收录了可爱女人、星晴等经典歌曲")
    private String description;
    
    /**
     * 专辑发布日期
     */
    @TableField("release_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "发布日期", example = "2000-11-07")
    private LocalDate releaseDate;
    
    /**
     * 专辑包含歌曲数量
     */
    @TableField("track_count")
    @Schema(description = "歌曲数量", example = "10")
    private Integer trackCount;
    
    /**
     * 播放次数
     */
    @TableField("play_count")
    @Schema(description = "播放次数", example = "158000000")
    private Long playCount;
    
    /**
     * 状态：0-禁用，1-正常
     */
    @TableField("status")
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;
    
    // ========================================
    // 关联对象（非数据库字段）
    // ========================================
    
    /**
     * 关联的艺术家信息
     */
    @TableField(exist = false)
    @Schema(description = "艺术家信息")
    private Artist artist;
}
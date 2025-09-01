package com.musicserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.musicserver.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 音乐标签实体类
 * 
 * 对应数据库music_tags表，存储音乐标签信息
 * 用于音乐的分类和标记
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("music_tags")
@Schema(description = "音乐标签实体")
public class MusicTag extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 标签名称
     */
    @TableField("name")
    @Schema(description = "标签名称", example = "怀旧", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    /**
     * 标签颜色，十六进制颜色值
     */
    @TableField("color")
    @Schema(description = "标签颜色", example = "#1890ff")
    private String color;
    
    /**
     * 标签描述
     */
    @TableField("description")
    @Schema(description = "标签描述", example = "怀旧风格的音乐")
    private String description;
    
    /**
     * 使用次数
     */
    @TableField("usage_count")
    @Schema(description = "使用次数", example = "1280")
    private Long usageCount;
    
    /**
     * 状态：0-禁用，1-正常
     */
    @TableField("status")
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;
}
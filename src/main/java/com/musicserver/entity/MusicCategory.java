package com.musicserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.musicserver.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 音乐分类实体类
 * 
 * 对应数据库music_categories表，存储音乐分类信息
 * 支持多级分类结构
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("music_categories")
@Schema(description = "音乐分类实体")
public class MusicCategory extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 分类名称
     */
    @TableField("name")
    @Schema(description = "分类名称", example = "流行", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    /**
     * 分类描述
     */
    @TableField("description")
    @Schema(description = "分类描述", example = "流行音乐类型")
    private String description;
    
    /**
     * 父分类ID，0表示顶级分类
     */
    @TableField("parent_id")
    @Schema(description = "父分类ID", example = "0")
    private Long parentId;
    
    /**
     * 排序顺序
     */
    @TableField("sort_order")
    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用，1-正常
     */
    @TableField("status")
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;
}
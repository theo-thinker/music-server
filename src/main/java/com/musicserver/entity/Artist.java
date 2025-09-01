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
 * 艺术家实体类
 * 
 * 对应数据库artists表，存储艺术家的基本信息
 * 包括姓名、简介、国籍、音乐风格等
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("artists")
@Schema(description = "艺术家信息实体")
public class Artist extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 艺术家姓名
     */
    @TableField("name")
    @Schema(description = "艺术家姓名", example = "周杰伦", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    /**
     * 艺术家头像URL
     */
    @TableField("avatar")
    @Schema(description = "艺术家头像", example = "/static/artists/jay_chou.jpg")
    private String avatar;
    
    /**
     * 艺术家描述信息
     */
    @TableField("description")
    @Schema(description = "艺术家描述", example = "华语流行音乐天王，创作才子")
    private String description;
    
    /**
     * 艺术家所属国家
     */
    @TableField("country")
    @Schema(description = "所属国家", example = "中国台湾")
    private String country;
    
    /**
     * 艺术家出生日期
     */
    @TableField("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "出生日期", example = "1979-01-18")
    private LocalDate birthDate;
    
    /**
     * 主要音乐风格
     */
    @TableField("genre")
    @Schema(description = "音乐风格", example = "流行,R&B,中国风")
    private String genre;
    
    /**
     * 关注者数量
     */
    @TableField("followers_count")
    @Schema(description = "关注者数量", example = "5280000")
    private Long followersCount;
    
    /**
     * 状态：0-禁用，1-正常
     */
    @TableField("status")
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;
}
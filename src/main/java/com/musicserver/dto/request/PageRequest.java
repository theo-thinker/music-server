package com.musicserver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 分页查询请求DTO
 * <p>
 * 用于接收分页查询的通用参数
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Schema(description = "分页查询请求")
public class PageRequest {

    /**
     * 页码，从1开始
     */
    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    @Schema(description = "每页大小", example = "20", defaultValue = "20")
    private Integer size = 20;

    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词", example = "青花瓷")
    private String keyword;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "createdTime")
    private String sortBy;

    /**
     * 排序方向：asc-升序，desc-降序
     */
    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDir = "desc";
}
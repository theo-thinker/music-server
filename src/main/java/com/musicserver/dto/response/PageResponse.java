package com.musicserver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应DTO
 * <p>
 * 用于返回分页查询结果的通用格式
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应")
public class PageResponse<T> {

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> records;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "1000")
    private Long total;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer size;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer current;

    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "50")
    private Long pages;

    /**
     * 是否有下一页
     */
    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;

    /**
     * 构造分页响应
     *
     * @param records 数据列表
     * @param total   总记录数
     * @param size    每页大小
     * @param current 当前页码
     */
    public PageResponse(List<T> records, Long total, Integer size, Integer current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = (total + size - 1) / size; // 计算总页数
        this.hasNext = current < pages;
        this.hasPrevious = current > 1;
    }

    /**
     * 创建空的分页响应
     *
     * @param <T> 数据类型
     * @return 空的分页响应
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 0L, 20, 1, 0L, false, false);
    }

    /**
     * 从MyBatis Plus的IPage创建分页响应
     *
     * @param page MyBatis Plus分页对象
     * @param <T>  数据类型
     * @return 分页响应
     */
    public static <T> PageResponse<T> of(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        return new PageResponse<>(
                page.getRecords(),
                page.getTotal(),
                (int) page.getSize(),
                (int) page.getCurrent(),
                page.getPages(),
                page.getCurrent() < page.getPages(), // hasNext
                page.getCurrent() > 1 // hasPrevious
        );
    }
}
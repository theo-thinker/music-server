package com.musicserver.ratelimit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 限流统计信息DTO
 * 
 * 封装限流监控的统计数据
 * 包含请求总数、通过数、拒绝数等统计信息
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitStatistics {

    /**
     * 统计类型 (hourly/daily)
     */
    private String type;

    /**
     * 统计时间
     */
    private String time;

    /**
     * 总请求数
     */
    private Long totalRequests;

    /**
     * 允许通过的请求数
     */
    private Long allowedRequests;

    /**
     * 被拒绝的请求数
     */
    private Long blockedRequests;

    /**
     * 热点请求数
     */
    private Long hotspotRequests;

    /**
     * 拒绝率（百分比）
     */
    private Double blockRate;

    /**
     * 热点率（百分比）
     */
    private Double hotspotRate;

    /**
     * 按策略统计
     */
    private Map<String, Long> strategyStats;

    /**
     * 按key统计
     */
    private Map<String, Long> keyStats;

    /**
     * 统计生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 创建空的统计信息
     * 
     * @param type 统计类型
     * @param time 统计时间
     * @return 空的统计信息
     */
    public static RateLimitStatistics empty(String type, String time) {
        return RateLimitStatistics.builder()
                .type(type)
                .time(time)
                .totalRequests(0L)
                .allowedRequests(0L)
                .blockedRequests(0L)
                .hotspotRequests(0L)
                .blockRate(0.0)
                .hotspotRate(0.0)
                .strategyStats(new HashMap<>())
                .keyStats(new HashMap<>())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 获取通过率
     * 
     * @return 通过率（百分比）
     */
    public Double getPassRate() {
        if (totalRequests == null || totalRequests == 0) {
            return 0.0;
        }
        return (allowedRequests != null ? allowedRequests : 0) * 100.0 / totalRequests;
    }

    /**
     * 判断是否有异常情况
     * 
     * @return 是否有异常
     */
    public boolean hasAnomalies() {
        // 拒绝率超过50%认为异常
        return blockRate != null && blockRate > 50.0;
    }

    /**
     * 获取最活跃的策略
     * 
     * @return 最活跃的策略名称
     */
    public String getMostActiveStrategy() {
        if (strategyStats == null || strategyStats.isEmpty()) {
            return null;
        }
        
        return strategyStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 获取最活跃的key
     * 
     * @return 最活跃的key
     */
    public String getMostActiveKey() {
        if (keyStats == null || keyStats.isEmpty()) {
            return null;
        }
        
        return keyStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 获取统计摘要
     * 
     * @return 统计摘要
     */
    public String getSummary() {
        return String.format(
            "类型: %s, 时间: %s, 总请求: %d, 通过: %d, 拒绝: %d, 拒绝率: %.2f%%, 热点: %d",
            type, time, totalRequests, allowedRequests, blockedRequests, blockRate, hotspotRequests
        );
    }
}
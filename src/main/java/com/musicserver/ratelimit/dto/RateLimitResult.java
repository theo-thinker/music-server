package com.musicserver.ratelimit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 限流结果DTO
 * 
 * 封装限流检查的结果信息
 * 包含是否通过、剩余配额、重置时间等详细信息
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitResult {

    /**
     * 是否允许通过
     */
    private boolean allowed;

    /**
     * 剩余配额
     */
    private long remaining;

    /**
     * 限流阈值
     */
    private long limit;

    /**
     * 当前计数
     */
    private long current;

    /**
     * 重置时间戳(毫秒)
     */
    private long resetTime;

    /**
     * 重置时间
     */
    private LocalDateTime resetDateTime;

    /**
     * 等待时间(毫秒)
     */
    private long waitTime;

    /**
     * 限流key
     */
    private String key;

    /**
     * 是否为热点数据
     */
    private boolean hotspot;

    /**
     * 热点级别(0-3)
     */
    private int hotspotLevel;

    /**
     * 错误码
     */
    private int errorCode;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 限流策略名称
     */
    private String strategy;

    /**
     * 限流类型名称
     */
    private String type;

    /**
     * 检查时间
     */
    private LocalDateTime checkTime;

    /**
     * 扩展信息
     */
    private Map<String, Object> extra;

    /**
     * 创建允许通过的结果
     * 
     * @param remaining 剩余配额
     * @param limit 限流阈值
     * @param resetTime 重置时间戳
     * @return 限流结果
     */
    public static RateLimitResult allowed(long remaining, long limit, long resetTime) {
        return RateLimitResult.builder()
                .allowed(true)
                .remaining(remaining)
                .limit(limit)
                .resetTime(resetTime)
                .resetDateTime(LocalDateTime.now().plusSeconds((resetTime - System.currentTimeMillis()) / 1000))
                .checkTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建拒绝通过的结果
     * 
     * @param limit 限流阈值
     * @param resetTime 重置时间戳
     * @param message 错误消息
     * @return 限流结果
     */
    public static RateLimitResult denied(long limit, long resetTime, String message) {
        return RateLimitResult.builder()
                .allowed(false)
                .remaining(0)
                .limit(limit)
                .resetTime(resetTime)
                .resetDateTime(LocalDateTime.now().plusSeconds((resetTime - System.currentTimeMillis()) / 1000))
                .message(message)
                .errorCode(429)
                .checkTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建热点拒绝结果
     * 
     * @param limit 限流阈值
     * @param resetTime 重置时间戳
     * @param hotspotLevel 热点级别
     * @param message 错误消息
     * @return 限流结果
     */
    public static RateLimitResult hotspotDenied(long limit, long resetTime, int hotspotLevel, String message) {
        return RateLimitResult.builder()
                .allowed(false)
                .remaining(0)
                .limit(limit)
                .resetTime(resetTime)
                .resetDateTime(LocalDateTime.now().plusSeconds((resetTime - System.currentTimeMillis()) / 1000))
                .hotspot(true)
                .hotspotLevel(hotspotLevel)
                .message(message)
                .errorCode(429)
                .checkTime(LocalDateTime.now())
                .build();
    }

    /**
     * 判断是否需要等待
     * 
     * @return 是否需要等待
     */
    public boolean needWait() {
        return !allowed && waitTime > 0;
    }

    /**
     * 获取剩余配额百分比
     * 
     * @return 剩余配额百分比
     */
    public double getRemainingPercentage() {
        if (limit <= 0) {
            return 0.0;
        }
        return (double) remaining / limit * 100.0;
    }

    /**
     * 获取使用配额百分比
     * 
     * @return 使用配额百分比
     */
    public double getUsagePercentage() {
        if (limit <= 0) {
            return 0.0;
        }
        return (double) current / limit * 100.0;
    }

    /**
     * 判断是否接近限流阈值
     * 
     * @param threshold 阈值百分比(0-1)
     * @return 是否接近限流阈值
     */
    public boolean isNearLimit(double threshold) {
        return getUsagePercentage() >= (threshold * 100);
    }

    /**
     * 获取距离重置的秒数
     * 
     * @return 距离重置的秒数
     */
    public long getSecondsToReset() {
        long now = System.currentTimeMillis();
        return Math.max(0, (resetTime - now) / 1000);
    }

    /**
     * 获取友好的等待时间描述
     * 
     * @return 等待时间描述
     */
    public String getWaitTimeDescription() {
        if (waitTime <= 0) {
            return "无需等待";
        }
        
        if (waitTime < 1000) {
            return waitTime + "毫秒";
        } else if (waitTime < 60000) {
            return (waitTime / 1000) + "秒";
        } else {
            return (waitTime / 60000) + "分钟";
        }
    }

    /**
     * 获取热点级别描述
     * 
     * @return 热点级别描述
     */
    public String getHotspotLevelDescription() {
        if (!hotspot) {
            return "非热点";
        }
        
        switch (hotspotLevel) {
            case 0:
                return "正常";
            case 1:
                return "轻度热点";
            case 2:
                return "中度热点";
            case 3:
                return "重度热点";
            default:
                return "未知级别";
        }
    }

    @Override
    public String toString() {
        return String.format(
            "RateLimitResult{allowed=%s, remaining=%d/%d, resetIn=%ds, hotspot=%s%s}",
            allowed, remaining, limit, getSecondsToReset(),
            hotspot ? "是" : "否",
            hotspot ? "(级别:" + hotspotLevel + ")" : ""
        );
    }
}
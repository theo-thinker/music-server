package com.musicserver.ratelimit.exception;

import com.musicserver.ratelimit.dto.RateLimitResult;
import lombok.Getter;

/**
 * 限流异常类
 * 
 * 当达到限流阈值时抛出的异常
 * 包含详细的限流信息和错误码
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Getter
public class RateLimitExceededException extends RuntimeException {

    /**
     * 错误码
     */
    private final int errorCode;

    /**
     * 限流结果详情
     */
    private final RateLimitResult rateLimitResult;

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public RateLimitExceededException(String message) {
        super(message);
        this.errorCode = 429;
        this.rateLimitResult = null;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param errorCode 错误码
     */
    public RateLimitExceededException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.rateLimitResult = null;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause 原因异常
     */
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 429;
        this.rateLimitResult = null;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param errorCode 错误码
     * @param rateLimitResult 限流结果
     */
    public RateLimitExceededException(String message, int errorCode, RateLimitResult rateLimitResult) {
        super(message);
        this.errorCode = errorCode;
        this.rateLimitResult = rateLimitResult;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param errorCode 错误码
     * @param cause 原因异常
     * @param rateLimitResult 限流结果
     */
    public RateLimitExceededException(String message, int errorCode, Throwable cause, RateLimitResult rateLimitResult) {
        super(message, cause);
        this.errorCode = errorCode;
        this.rateLimitResult = rateLimitResult;
    }

    /**
     * 获取剩余配额
     *
     * @return 剩余配额，如果没有限流结果则返回0
     */
    public long getRemaining() {
        return rateLimitResult != null ? rateLimitResult.getRemaining() : 0;
    }

    /**
     * 获取限流阈值
     *
     * @return 限流阈值，如果没有限流结果则返回0
     */
    public long getLimit() {
        return rateLimitResult != null ? rateLimitResult.getLimit() : 0;
    }

    /**
     * 获取重置时间戳
     *
     * @return 重置时间戳，如果没有限流结果则返回当前时间
     */
    public long getResetTime() {
        return rateLimitResult != null ? rateLimitResult.getResetTime() : System.currentTimeMillis();
    }

    /**
     * 获取等待时间（毫秒）
     *
     * @return 等待时间
     */
    public long getWaitTime() {
        return rateLimitResult != null ? rateLimitResult.getWaitTime() : 0;
    }

    /**
     * 是否为热点数据导致的限流
     *
     * @return 是否为热点限流
     */
    public boolean isHotspot() {
        return rateLimitResult != null && rateLimitResult.isHotspot();
    }

    /**
     * 获取热点级别
     *
     * @return 热点级别
     */
    public int getHotspotLevel() {
        return rateLimitResult != null ? rateLimitResult.getHotspotLevel() : 0;
    }

    /**
     * 获取距离重置的秒数
     *
     * @return 距离重置的秒数
     */
    public long getSecondsToReset() {
        return rateLimitResult != null ? rateLimitResult.getSecondsToReset() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RateLimitExceededException{");
        sb.append("message='").append(getMessage()).append('\'');
        sb.append(", errorCode=").append(errorCode);
        
        if (rateLimitResult != null) {
            sb.append(", remaining=").append(getRemaining());
            sb.append(", limit=").append(getLimit());
            sb.append(", resetIn=").append(getSecondsToReset()).append("s");
            
            if (isHotspot()) {
                sb.append(", hotspot=true");
                sb.append(", hotspotLevel=").append(getHotspotLevel());
            }
        }
        
        sb.append('}');
        return sb.toString();
    }
}
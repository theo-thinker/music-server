package com.musicserver.ratelimit.exception;

/**
 * 限流配置异常类
 * 
 * 当限流配置不正确时抛出的异常
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class RateLimitConfigException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public RateLimitConfigException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause 原因异常
     */
    public RateLimitConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param cause 原因异常
     */
    public RateLimitConfigException(Throwable cause) {
        super(cause);
    }
}
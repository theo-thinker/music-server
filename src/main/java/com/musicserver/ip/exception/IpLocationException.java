package com.musicserver.ip.exception;

/**
 * IP定位异常类
 * 
 * 用于处理IP定位相关的异常情况
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class IpLocationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 详细信息
     */
    private Object details;

    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public IpLocationException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public IpLocationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param message 错误消息
     */
    public IpLocationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param cause 原因
     */
    public IpLocationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param details 详细信息
     */
    public IpLocationException(String errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param details 详细信息
     * @param cause 原因
     */
    public IpLocationException(String errorCode, String message, Object details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 设置错误代码
     * 
     * @param errorCode 错误代码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取详细信息
     * 
     * @return 详细信息
     */
    public Object getDetails() {
        return details;
    }

    /**
     * 设置详细信息
     * 
     * @param details 详细信息
     */
    public void setDetails(Object details) {
        this.details = details;
    }

    // 常用的静态工厂方法

    /**
     * 创建数据库不存在异常
     * 
     * @param databasePath 数据库路径
     * @return 异常实例
     */
    public static IpLocationException databaseNotFound(String databasePath) {
        return new IpLocationException("IP_DB_NOT_FOUND", 
                "IP2Region database not found: " + databasePath, databasePath);
    }

    /**
     * 创建数据库加载失败异常
     * 
     * @param cause 原因
     * @return 异常实例
     */
    public static IpLocationException databaseLoadFailed(Throwable cause) {
        return new IpLocationException("IP_DB_LOAD_FAILED", 
                "Failed to load IP2Region database", cause);
    }

    /**
     * 创建查询超时异常
     * 
     * @param ip IP地址
     * @param timeout 超时时间
     * @return 异常实例
     */
    public static IpLocationException queryTimeout(String ip, long timeout) {
        return new IpLocationException("IP_QUERY_TIMEOUT", 
                String.format("IP location query timeout for %s (timeout: %dms)", ip, timeout), 
                ip);
    }

    /**
     * 创建查询失败异常
     * 
     * @param ip IP地址
     * @param cause 原因
     * @return 异常实例
     */
    public static IpLocationException queryFailed(String ip, Throwable cause) {
        return new IpLocationException("IP_QUERY_FAILED", 
                "Failed to query IP location for: " + ip, ip, cause);
    }

    /**
     * 创建缓存操作失败异常
     * 
     * @param operation 操作类型
     * @param cause 原因
     * @return 异常实例
     */
    public static IpLocationException cacheOperationFailed(String operation, Throwable cause) {
        return new IpLocationException("IP_CACHE_FAILED", 
                "IP location cache operation failed: " + operation, operation, cause);
    }

    /**
     * 创建统计操作失败异常
     * 
     * @param operation 操作类型
     * @param cause 原因
     * @return 异常实例
     */
    public static IpLocationException statisticsOperationFailed(String operation, Throwable cause) {
        return new IpLocationException("IP_STATS_FAILED", 
                "IP statistics operation failed: " + operation, operation, cause);
    }

    /**
     * 创建配置错误异常
     * 
     * @param configName 配置名称
     * @param configValue 配置值
     * @return 异常实例
     */
    public static IpLocationException invalidConfiguration(String configName, Object configValue) {
        return new IpLocationException("IP_CONFIG_INVALID", 
                String.format("Invalid IP location configuration: %s = %s", configName, configValue), 
                configValue);
    }

    /**
     * 创建服务不可用异常
     * 
     * @param reason 原因
     * @return 异常实例
     */
    public static IpLocationException serviceUnavailable(String reason) {
        return new IpLocationException("IP_SERVICE_UNAVAILABLE", 
                "IP location service is unavailable: " + reason, reason);
    }

    /**
     * 创建限流异常
     * 
     * @param ip IP地址
     * @param limit 限流值
     * @return 异常实例
     */
    public static IpLocationException rateLimitExceeded(String ip, int limit) {
        return new IpLocationException("IP_RATE_LIMIT_EXCEEDED", 
                String.format("Rate limit exceeded for IP %s (limit: %d)", ip, limit), 
                ip);
    }

    /**
     * 创建访问被拒绝异常
     * 
     * @param ip IP地址
     * @param reason 拒绝原因
     * @return 异常实例
     */
    public static IpLocationException accessDenied(String ip, String reason) {
        return new IpLocationException("IP_ACCESS_DENIED", 
                String.format("Access denied for IP %s: %s", ip, reason), 
                ip);
    }
}
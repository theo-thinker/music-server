package com.musicserver.ip.exception;

/**
 * IP验证异常类
 * 
 * 用于处理IP地址验证相关的异常情况
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class IpValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 无效的IP地址
     */
    private String invalidIp;

    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public IpValidationException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public IpValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param message 错误消息
     */
    public IpValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param invalidIp 无效的IP地址
     */
    public IpValidationException(String errorCode, String message, String invalidIp) {
        super(message);
        this.errorCode = errorCode;
        this.invalidIp = invalidIp;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param invalidIp 无效的IP地址
     * @param cause 原因
     */
    public IpValidationException(String errorCode, String message, String invalidIp, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.invalidIp = invalidIp;
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
     * 获取无效的IP地址
     * 
     * @return 无效的IP地址
     */
    public String getInvalidIp() {
        return invalidIp;
    }

    /**
     * 设置无效的IP地址
     * 
     * @param invalidIp 无效的IP地址
     */
    public void setInvalidIp(String invalidIp) {
        this.invalidIp = invalidIp;
    }

    // 常用的静态工厂方法

    /**
     * 创建无效IP格式异常
     * 
     * @param ip 无效的IP地址
     * @return 异常实例
     */
    public static IpValidationException invalidFormat(String ip) {
        return new IpValidationException("IP_INVALID_FORMAT", 
                "Invalid IP address format: " + ip, ip);
    }

    /**
     * 创建IP为空异常
     * 
     * @return 异常实例
     */
    public static IpValidationException nullOrEmpty() {
        return new IpValidationException("IP_NULL_OR_EMPTY", 
                "IP address cannot be null or empty", null);
    }

    /**
     * 创建IPv4格式错误异常
     * 
     * @param ip 无效的IPv4地址
     * @return 异常实例
     */
    public static IpValidationException invalidIPv4(String ip) {
        return new IpValidationException("IP_INVALID_IPV4", 
                "Invalid IPv4 address: " + ip, ip);
    }

    /**
     * 创建IPv6格式错误异常
     * 
     * @param ip 无效的IPv6地址
     * @return 异常实例
     */
    public static IpValidationException invalidIPv6(String ip) {
        return new IpValidationException("IP_INVALID_IPV6", 
                "Invalid IPv6 address: " + ip, ip);
    }

    /**
     * 创建IP范围错误异常
     * 
     * @param ip 超出范围的IP地址
     * @return 异常实例
     */
    public static IpValidationException outOfRange(String ip) {
        return new IpValidationException("IP_OUT_OF_RANGE", 
                "IP address out of valid range: " + ip, ip);
    }

    /**
     * 创建保留IP地址异常
     * 
     * @param ip 保留的IP地址
     * @return 异常实例
     */
    public static IpValidationException reservedAddress(String ip) {
        return new IpValidationException("IP_RESERVED_ADDRESS", 
                "Reserved IP address not allowed: " + ip, ip);
    }

    /**
     * 创建私有IP地址异常
     * 
     * @param ip 私有IP地址
     * @return 异常实例
     */
    public static IpValidationException privateAddress(String ip) {
        return new IpValidationException("IP_PRIVATE_ADDRESS", 
                "Private IP address not allowed: " + ip, ip);
    }

    /**
     * 创建广播地址异常
     * 
     * @param ip 广播地址
     * @return 异常实例
     */
    public static IpValidationException broadcastAddress(String ip) {
        return new IpValidationException("IP_BROADCAST_ADDRESS", 
                "Broadcast address not allowed: " + ip, ip);
    }

    /**
     * 创建多播地址异常
     * 
     * @param ip 多播地址
     * @return 异常实例
     */
    public static IpValidationException multicastAddress(String ip) {
        return new IpValidationException("IP_MULTICAST_ADDRESS", 
                "Multicast address not allowed: " + ip, ip);
    }

    /**
     * 创建回环地址异常
     * 
     * @param ip 回环地址
     * @return 异常实例
     */
    public static IpValidationException loopbackAddress(String ip) {
        return new IpValidationException("IP_LOOPBACK_ADDRESS", 
                "Loopback address not allowed in this context: " + ip, ip);
    }

    /**
     * 创建黑名单IP异常
     * 
     * @param ip 黑名单IP地址
     * @return 异常实例
     */
    public static IpValidationException blacklisted(String ip) {
        return new IpValidationException("IP_BLACKLISTED", 
                "IP address is blacklisted: " + ip, ip);
    }

    /**
     * 创建IP解析失败异常
     * 
     * @param ip 无法解析的IP地址
     * @param cause 原因
     * @return 异常实例
     */
    public static IpValidationException resolutionFailed(String ip, Throwable cause) {
        return new IpValidationException("IP_RESOLUTION_FAILED", 
                "Failed to resolve IP address: " + ip, ip, cause);
    }

    /**
     * 创建CIDR格式错误异常
     * 
     * @param cidr 无效的CIDR
     * @return 异常实例
     */
    public static IpValidationException invalidCIDR(String cidr) {
        return new IpValidationException("IP_INVALID_CIDR", 
                "Invalid CIDR format: " + cidr, cidr);
    }

    /**
     * 创建IP长度错误异常
     * 
     * @param ip IP地址
     * @param expectedLength 期望长度
     * @param actualLength 实际长度
     * @return 异常实例
     */
    public static IpValidationException invalidLength(String ip, int expectedLength, int actualLength) {
        return new IpValidationException("IP_INVALID_LENGTH", 
                String.format("Invalid IP address length: %s (expected: %d, actual: %d)", 
                        ip, expectedLength, actualLength), ip);
    }

    /**
     * 创建IP版本不匹配异常
     * 
     * @param ip IP地址
     * @param expectedVersion 期望版本
     * @param actualVersion 实际版本
     * @return 异常实例
     */
    public static IpValidationException versionMismatch(String ip, String expectedVersion, String actualVersion) {
        return new IpValidationException("IP_VERSION_MISMATCH", 
                String.format("IP version mismatch: %s (expected: %s, actual: %s)", 
                        ip, expectedVersion, actualVersion), ip);
    }
}
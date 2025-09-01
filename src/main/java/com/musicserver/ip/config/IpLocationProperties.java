package com.musicserver.ip.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * IP定位服务配置属性类
 * 
 * 管理IP定位模块的所有配置参数
 * 支持缓存配置、性能优化、安全设置等
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@ConfigurationProperties(prefix = "ip-location")
public class IpLocationProperties {

    /**
     * 是否启用IP定位功能
     */
    private Boolean enabled = true;

    /**
     * 是否启用缓存
     */
    private Boolean enableCache = true;

    /**
     * 是否启用统计功能
     */
    private Boolean enableStatistics = true;

    /**
     * 是否启用IP拦截器
     */
    private Boolean enableInterceptor = true;

    /**
     * ip2region数据库文件路径
     */
    private String databasePath = "ip2region/ip2region.xdb";

    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 统计配置
     */
    private StatisticsConfig statistics = new StatisticsConfig();

    /**
     * 性能配置
     */
    private PerformanceConfig performance = new PerformanceConfig();

    /**
     * 安全配置
     */
    private SecurityConfig security = new SecurityConfig();

    /**
     * 限流配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * 缓存配置内部类
     */
    @Data
    public static class CacheConfig {
        /**
         * 缓存过期时间
         */
        private Long expireTime = 24L;

        /**
         * 缓存时间单位
         */
        private TimeUnit timeUnit = TimeUnit.HOURS;

        /**
         * 最大缓存数量
         */
        private Integer maxSize = 10000;

        /**
         * 缓存key前缀
         */
        private String keyPrefix = "ip_location:";

        /**
         * 是否启用本地缓存
         */
        private Boolean enableLocalCache = true;

        /**
         * 本地缓存最大数量
         */
        private Integer localCacheMaxSize = 1000;

        /**
         * 本地缓存过期时间（分钟）
         */
        private Long localCacheExpireMinutes = 30L;
    }

    /**
     * 统计配置内部类
     */
    @Data
    public static class StatisticsConfig {
        /**
         * 统计数据保留天数
         */
        private Integer retentionDays = 30;

        /**
         * 是否启用实时统计
         */
        private Boolean enableRealTime = true;

        /**
         * 统计批量大小
         */
        private Integer batchSize = 100;

        /**
         * 统计刷新间隔（秒）
         */
        private Long flushInterval = 60L;

        /**
         * 是否启用异步统计
         */
        private Boolean enableAsync = true;

        /**
         * 异步线程池大小
         */
        private Integer asyncThreadPoolSize = 4;
    }

    /**
     * 性能配置内部类
     */
    @Data
    public static class PerformanceConfig {
        /**
         * 查询超时时间（毫秒）
         */
        private Long queryTimeout = 5000L;

        /**
         * 是否启用查询结果预加载
         */
        private Boolean enablePreload = false;

        /**
         * 预加载IP段数量
         */
        private Integer preloadCount = 1000;

        /**
         * 是否启用异步查询
         */
        private Boolean enableAsyncQuery = false;

        /**
         * 异步查询线程池大小
         */
        private Integer asyncThreadPoolSize = 8;

        /**
         * 最大并发查询数
         */
        private Integer maxConcurrentQueries = 100;
    }

    /**
     * 安全配置内部类
     */
    @Data
    public static class SecurityConfig {
        /**
         * 是否启用IP白名单
         */
        private Boolean enableWhitelist = false;

        /**
         * IP白名单
         */
        private String[] whitelist = {};

        /**
         * 是否启用IP黑名单
         */
        private Boolean enableBlacklist = false;

        /**
         * IP黑名单
         */
        private String[] blacklist = {};

        /**
         * 是否启用内网IP过滤
         */
        private Boolean filterPrivateIp = false;

        /**
         * 是否启用风险IP检测
         */
        private Boolean enableRiskDetection = true;

        /**
         * 风险IP检测阈值
         */
        private Integer riskThreshold = 1000;

        /**
         * 是否启用自动封禁
         */
        private Boolean enableAutoBan = false;

        /**
         * 自动封禁阈值
         */
        private Integer autoBanThreshold = 10000;

        /**
         * 封禁时长（小时）
         */
        private Integer banDurationHours = 24;
    }

    /**
     * 限流配置内部类
     */
    @Data
    public static class RateLimitConfig {
        /**
         * 是否启用查询限流
         */
        private Boolean enableQueryRateLimit = true;

        /**
         * 每IP查询限制（次/分钟）
         */
        private Integer queryLimitPerIp = 60;

        /**
         * 全局查询限制（次/分钟）
         */
        private Integer globalQueryLimit = 10000;

        /**
         * 限流时间窗口（秒）
         */
        private Integer rateLimitWindow = 60;

        /**
         * 是否启用突发查询控制
         */
        private Boolean enableBurstControl = true;

        /**
         * 突发查询阈值
         */
        private Integer burstThreshold = 100;
    }

    /**
     * 验证配置是否有效
     * 
     * @return 配置是否有效
     */
    public boolean isValid() {
        return enabled != null && databasePath != null && !databasePath.trim().isEmpty();
    }

    /**
     * 获取缓存键前缀
     * 
     * @return 缓存键前缀
     */
    public String getCacheKeyPrefix() {
        return cache.keyPrefix != null ? cache.keyPrefix : "ip_location:";
    }

    /**
     * 获取缓存过期时间（秒）
     * 
     * @return 缓存过期时间
     */
    public long getCacheExpireSeconds() {
        if (cache.expireTime == null || cache.timeUnit == null) {
            return 24 * 60 * 60; // 默认24小时
        }
        return cache.timeUnit.toSeconds(cache.expireTime);
    }

    /**
     * 是否启用安全功能
     * 
     * @return 是否启用安全功能
     */
    public boolean isSecurityEnabled() {
        return security.enableWhitelist || security.enableBlacklist || 
               security.enableRiskDetection || security.enableAutoBan;
    }

    /**
     * 是否为白名单IP
     * 
     * @param ip IP地址
     * @return 是否为白名单IP
     */
    public boolean isWhitelistIp(String ip) {
        if (!security.enableWhitelist || security.whitelist == null) {
            return false;
        }
        
        for (String whiteIp : security.whitelist) {
            if (ip.equals(whiteIp) || ip.startsWith(whiteIp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否为黑名单IP
     * 
     * @param ip IP地址
     * @return 是否为黑名单IP
     */
    public boolean isBlacklistIp(String ip) {
        if (!security.enableBlacklist || security.blacklist == null) {
            return false;
        }
        
        for (String blackIp : security.blacklist) {
            if (ip.equals(blackIp) || ip.startsWith(blackIp)) {
                return true;
            }
        }
        return false;
    }
}
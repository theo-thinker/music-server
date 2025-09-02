package com.musicserver.ratelimit.config;

import com.musicserver.ratelimit.enums.RateLimitStrategy;
import com.musicserver.ratelimit.enums.RateLimitType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 限流配置属性类
 * <p>
 * 管理系统级别的限流配置参数
 * 支持多维度、多策略的限流配置
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /**
     * 是否启用限流功能
     */
    private Boolean enabled = true;

    /**
     * 默认限流策略
     */
    private RateLimitStrategy defaultStrategy = RateLimitStrategy.SLIDING_WINDOW;

    /**
     * 默认限流类型
     */
    private RateLimitType defaultType = RateLimitType.GLOBAL;

    /**
     * 默认限流次数
     */
    private Long defaultLimit = 100L;

    /**
     * 默认时间窗口大小
     */
    private Long defaultPeriod = 60L;

    /**
     * 默认时间单位
     */
    private TimeUnit defaultTimeUnit = TimeUnit.SECONDS;

    /**
     * 是否启用限流日志
     */
    private Boolean enableLog = true;

    /**
     * 是否启用限流监控
     */
    private Boolean enableMonitor = true;

    /**
     * 限流key前缀
     */
    private String keyPrefix = "rate_limit";

    /**
     * Redis相关配置
     */
    private RedisConfig redis = new RedisConfig();

    /**
     * 各种策略的默认配置
     */
    private StrategyConfig strategy = new StrategyConfig();

    /**
     * 全局限流配置
     */
    private GlobalConfig global = new GlobalConfig();

    /**
     * IP限流配置
     */
    private IpConfig ip = new IpConfig();

    /**
     * 用户限流配置
     */
    private UserConfig user = new UserConfig();

    /**
     * API限流配置
     */
    private ApiConfig api = new ApiConfig();

    /**
     * 热点数据限流配置
     */
    private HotspotConfig hotspot = new HotspotConfig();

    /**
     * 自定义限流规则
     */
    private Map<String, CustomRuleConfig> customRules = new HashMap<>();

    /**
     * Redis配置内部类
     */
    @Data
    public static class RedisConfig {
        /**
         * Redis Key过期时间(秒)
         */
        private Long expireTime = 7200L;

        /**
         * Lua脚本缓存时间(秒)
         */
        private Long scriptCacheTime = 3600L;

        /**
         * Redis连接超时时间(毫秒)
         */
        private Long connectionTimeout = 5000L;

        /**
         * Redis执行超时时间(毫秒)
         */
        private Long commandTimeout = 3000L;

        /**
         * 是否启用Redis管道
         */
        private Boolean enablePipeline = false;

        /**
         * 管道批量大小
         */
        private Integer pipelineBatchSize = 100;
    }

    /**
     * 策略配置内部类
     */
    @Data
    public static class StrategyConfig {
        /**
         * 滑动窗口配置
         */
        private SlidingWindowConfig slidingWindow = new SlidingWindowConfig();

        /**
         * 令牌桶配置
         */
        private TokenBucketConfig tokenBucket = new TokenBucketConfig();

        /**
         * 漏桶配置
         */
        private LeakyBucketConfig leakyBucket = new LeakyBucketConfig();

        /**
         * 固定窗口配置
         */
        private FixedWindowConfig fixedWindow = new FixedWindowConfig();

        /**
         * 计数器配置
         */
        private CounterConfig counter = new CounterConfig();

        @Data
        public static class SlidingWindowConfig {
            /**
             * 默认窗口分片数量
             */
            private Integer defaultSlices = 60;

            /**
             * 最大分片数量
             */
            private Integer maxSlices = 3600;

            /**
             * 最小分片数量
             */
            private Integer minSlices = 10;
        }

        @Data
        public static class TokenBucketConfig {
            /**
             * 默认桶容量
             */
            private Long defaultCapacity = 100L;

            /**
             * 默认令牌生成速率(令牌/秒)
             */
            private Double defaultRefillRate = 10.0;

            /**
             * 默认预热时间(秒)
             */
            private Long defaultWarmupPeriod = 0L;

            /**
             * 最大桶容量
             */
            private Long maxCapacity = 10000L;

            /**
             * 最大令牌生成速率
             */
            private Double maxRefillRate = 1000.0;
        }

        @Data
        public static class LeakyBucketConfig {
            /**
             * 默认桶容量
             */
            private Long defaultCapacity = 100L;

            /**
             * 默认漏出速率(请求/秒)
             */
            private Double defaultLeakRate = 10.0;

            /**
             * 最大桶容量
             */
            private Long maxCapacity = 10000L;

            /**
             * 最大漏出速率
             */
            private Double maxLeakRate = 1000.0;
        }

        @Data
        public static class FixedWindowConfig {
            /**
             * 默认窗口大小(秒)
             */
            private Long defaultWindowSize = 60L;

            /**
             * 最大窗口大小(秒)
             */
            private Long maxWindowSize = 3600L;

            /**
             * 最小窗口大小(秒)
             */
            private Long minWindowSize = 1L;
        }

        @Data
        public static class CounterConfig {
            /**
             * 默认重置间隔(秒)
             */
            private Long defaultResetInterval = 60L;

            /**
             * 最大重置间隔(秒)
             */
            private Long maxResetInterval = 86400L;

            /**
             * 最小重置间隔(秒)
             */
            private Long minResetInterval = 1L;
        }
    }

    /**
     * 全局限流配置
     */
    @Data
    public static class GlobalConfig {
        /**
         * 是否启用全局限流
         */
        private Boolean enabled = true;

        /**
         * 全局限流阈值
         */
        private Long limit = 10000L;

        /**
         * 全局限流时间窗口(秒)
         */
        private Long period = 60L;

        /**
         * 全局限流策略
         */
        private RateLimitStrategy strategy = RateLimitStrategy.SLIDING_WINDOW;
    }

    /**
     * IP限流配置
     */
    @Data
    public static class IpConfig {
        /**
         * 是否启用IP限流
         */
        private Boolean enabled = true;

        /**
         * IP限流阈值
         */
        private Long limit = 1000L;

        /**
         * IP限流时间窗口(秒)
         */
        private Long period = 60L;

        /**
         * IP限流策略
         */
        private RateLimitStrategy strategy = RateLimitStrategy.SLIDING_WINDOW;

        /**
         * IP白名单
         */
        private String[] whitelist = {"127.0.0.1", "::1"};

        /**
         * IP黑名单
         */
        private String[] blacklist = {};

        /**
         * 是否启用地理位置限流
         */
        private Boolean enableGeoLimit = false;

        /**
         * 允许的国家代码
         */
        private String[] allowedCountries = {};

        /**
         * 禁止的国家代码
         */
        private String[] blockedCountries = {};
    }

    /**
     * 用户限流配置
     */
    @Data
    public static class UserConfig {
        /**
         * 是否启用用户限流
         */
        private Boolean enabled = true;

        /**
         * 用户限流阈值
         */
        private Long limit = 500L;

        /**
         * 用户限流时间窗口(秒)
         */
        private Long period = 60L;

        /**
         * 用户限流策略
         */
        private RateLimitStrategy strategy = RateLimitStrategy.TOKEN_BUCKET;

        /**
         * 不同角色的限流配置
         */
        private Map<String, RoleConfig> roles = new HashMap<>();

        @Data
        public static class RoleConfig {
            /**
             * 角色限流阈值
             */
            private Long limit;

            /**
             * 角色限流时间窗口(秒)
             */
            private Long period;

            /**
             * 角色限流策略
             */
            private RateLimitStrategy strategy;
        }
    }

    /**
     * API限流配置
     */
    @Data
    public static class ApiConfig {
        /**
         * 是否启用API限流
         */
        private Boolean enabled = true;

        /**
         * API限流阈值
         */
        private Long limit = 200L;

        /**
         * API限流时间窗口(秒)
         */
        private Long period = 60L;

        /**
         * API限流策略
         */
        private RateLimitStrategy strategy = RateLimitStrategy.FIXED_WINDOW;

        /**
         * 特定API的限流配置
         */
        private Map<String, SpecificApiConfig> specificApis = new HashMap<>();

        @Data
        public static class SpecificApiConfig {
            /**
             * API路径模式
             */
            private String pattern;

            /**
             * API限流阈值
             */
            private Long limit;

            /**
             * API限流时间窗口(秒)
             */
            private Long period;

            /**
             * API限流策略
             */
            private RateLimitStrategy strategy;

            /**
             * 是否启用
             */
            private Boolean enabled = true;
        }
    }

    /**
     * 热点数据限流配置
     */
    @Data
    public static class HotspotConfig {
        /**
         * 是否启用热点限流
         */
        private Boolean enabled = false;

        /**
         * 热点检测阈值
         */
        private Long detectionThreshold = 200L;

        /**
         * 热点检测时间窗口(秒)
         */
        private Long detectionWindow = 300L;

        /**
         * 热点限流阈值
         */
        private Long hotspotLimit = 50L;

        /**
         * 热点限流时间窗口(秒)
         */
        private Long hotspotPeriod = 60L;

        /**
         * 热点数据过期时间(秒)
         */
        private Long hotspotExpireTime = 3600L;

        /**
         * 最大热点数据数量
         */
        private Integer maxHotspots = 1000;
    }

    /**
     * 自定义限流规则配置
     */
    @Data
    public static class CustomRuleConfig {
        /**
         * 规则名称
         */
        private String name;

        /**
         * 限流key表达式
         */
        private String keyExpression;

        /**
         * 限流阈值
         */
        private Long limit;

        /**
         * 限流时间窗口(秒)
         */
        private Long period;

        /**
         * 限流策略
         */
        private RateLimitStrategy strategy;

        /**
         * 限流类型
         */
        private RateLimitType type;

        /**
         * 条件表达式
         */
        private String condition;

        /**
         * 是否启用
         */
        private Boolean enabled = true;

        /**
         * 优先级
         */
        private Integer priority = 0;

        /**
         * 描述
         */
        private String description;
    }

    /**
     * 验证配置是否有效
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        return enabled != null &&
                defaultLimit != null && defaultLimit > 0 &&
                defaultPeriod != null && defaultPeriod > 0 &&
                defaultStrategy != null &&
                defaultType != null &&
                keyPrefix != null && !keyPrefix.trim().isEmpty();
    }

    /**
     * 获取指定策略的默认配置
     *
     * @param strategy 限流策略
     * @return 默认配置映射
     */
    public Map<String, Object> getStrategyDefaults(RateLimitStrategy strategy) {
        Map<String, Object> defaults = new HashMap<>();

        switch (strategy) {
            case SLIDING_WINDOW:
                defaults.put("slices", this.strategy.slidingWindow.defaultSlices);
                defaults.put("maxSlices", this.strategy.slidingWindow.maxSlices);
                defaults.put("minSlices", this.strategy.slidingWindow.minSlices);
                break;

            case TOKEN_BUCKET:
            case DISTRIBUTED_TOKEN_BUCKET:
                defaults.put("capacity", this.strategy.tokenBucket.defaultCapacity);
                defaults.put("refillRate", this.strategy.tokenBucket.defaultRefillRate);
                defaults.put("warmupPeriod", this.strategy.tokenBucket.defaultWarmupPeriod);
                defaults.put("maxCapacity", this.strategy.tokenBucket.maxCapacity);
                defaults.put("maxRefillRate", this.strategy.tokenBucket.maxRefillRate);
                break;

            case LEAKY_BUCKET:
                defaults.put("capacity", this.strategy.leakyBucket.defaultCapacity);
                defaults.put("leakRate", this.strategy.leakyBucket.defaultLeakRate);
                defaults.put("maxCapacity", this.strategy.leakyBucket.maxCapacity);
                defaults.put("maxLeakRate", this.strategy.leakyBucket.maxLeakRate);
                break;

            case FIXED_WINDOW:
                defaults.put("windowSize", this.strategy.fixedWindow.defaultWindowSize);
                defaults.put("maxWindowSize", this.strategy.fixedWindow.maxWindowSize);
                defaults.put("minWindowSize", this.strategy.fixedWindow.minWindowSize);
                break;

            case COUNTER:
                defaults.put("resetInterval", this.strategy.counter.defaultResetInterval);
                defaults.put("maxResetInterval", this.strategy.counter.maxResetInterval);
                defaults.put("minResetInterval", this.strategy.counter.minResetInterval);
                break;

            default:
                break;
        }

        return defaults;
    }

    /**
     * 获取指定类型的限流配置
     *
     * @param type 限流类型
     * @return 限流配置
     */
    public Map<String, Object> getTypeConfig(RateLimitType type) {
        Map<String, Object> config = new HashMap<>();

        switch (type) {
            case GLOBAL:
                config.put("enabled", global.enabled);
                config.put("limit", global.limit);
                config.put("period", global.period);
                config.put("strategy", global.strategy);
                break;

            case IP:
                config.put("enabled", ip.enabled);
                config.put("limit", ip.limit);
                config.put("period", ip.period);
                config.put("strategy", ip.strategy);
                config.put("whitelist", ip.whitelist);
                config.put("blacklist", ip.blacklist);
                break;

            case USER:
                config.put("enabled", user.enabled);
                config.put("limit", user.limit);
                config.put("period", user.period);
                config.put("strategy", user.strategy);
                config.put("roles", user.roles);
                break;

            case API:
                config.put("enabled", api.enabled);
                config.put("limit", api.limit);
                config.put("period", api.period);
                config.put("strategy", api.strategy);
                config.put("specificApis", api.specificApis);
                break;

            default:
                // 使用默认配置
                config.put("enabled", true);
                config.put("limit", defaultLimit);
                config.put("period", defaultPeriod);
                config.put("strategy", defaultStrategy);
                break;
        }

        return config;
    }
}
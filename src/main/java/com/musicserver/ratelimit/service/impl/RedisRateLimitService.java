package com.musicserver.ratelimit.service.impl;

import com.musicserver.ratelimit.annotation.RateLimit;
import com.musicserver.ratelimit.config.RateLimitProperties;
import com.musicserver.ratelimit.dto.RateLimitResult;
import com.musicserver.ratelimit.enums.RateLimitStrategy;
import com.musicserver.ratelimit.enums.RateLimitType;
import com.musicserver.ratelimit.monitor.RateLimitMonitor;
import com.musicserver.ratelimit.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis限流服务实现类
 * 
 * 基于Redis和Lua脚本实现的分布式限流服务
 * 支持多种限流策略和企业级功能
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Service
public class RedisRateLimitService implements RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;
    private final RateLimitMonitor monitor;
    
    // Lua脚本缓存
    private final Map<RateLimitStrategy, DefaultRedisScript<List>> scriptCache = new ConcurrentHashMap<>();
    
    // 活跃限流器缓存
    private final Set<String> activeKeys = ConcurrentHashMap.newKeySet();

    public RedisRateLimitService(StringRedisTemplate redisTemplate, RateLimitProperties properties, RateLimitMonitor monitor) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.monitor = monitor;
    }

    /**
     * 初始化Lua脚本
     */
    @PostConstruct
    public void initScripts() {
        try {
            log.info("Initializing Rate Limit Lua scripts...");
            
            // 加载各种策略的Lua脚本
            loadScript(RateLimitStrategy.SLIDING_WINDOW, "lua/sliding_window.lua");
            loadScript(RateLimitStrategy.TOKEN_BUCKET, "lua/token_bucket.lua");
            loadScript(RateLimitStrategy.LEAKY_BUCKET, "lua/leaky_bucket.lua");
            loadScript(RateLimitStrategy.FIXED_WINDOW, "lua/fixed_window.lua");
            loadScript(RateLimitStrategy.COUNTER, "lua/counter.lua");
            loadScript(RateLimitStrategy.DISTRIBUTED_TOKEN_BUCKET, "lua/distributed_token_bucket.lua");
            loadScript(RateLimitStrategy.HOTSPOT, "lua/hotspot.lua");
            
            log.info("Rate Limit Lua scripts initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Lua scripts", e);
            throw new RuntimeException("Lua scripts initialization failed", e);
        }
    }

    /**
     * 加载Lua脚本
     */
    private void loadScript(RateLimitStrategy strategy, String scriptPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        String scriptContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(scriptContent);
        script.setResultType(List.class);
        
        scriptCache.put(strategy, script);
        log.debug("Loaded Lua script for strategy: {}", strategy);
    }

    @Override
    public RateLimitResult tryAcquire(RateLimit rateLimit, String key, Map<String, Object> context) {
        // 检查是否启用限流
        if (!properties.getEnabled() || !rateLimit.enabled()) {
            return RateLimitResult.allowed(rateLimit.limit(), rateLimit.limit(), 
                                         System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(rateLimit.period()));
        }

        // 构建完整的限流key
        String fullKey = buildRateLimitKey(rateLimit, key, context);
        
        // 记录活跃key
        activeKeys.add(fullKey);

        try {
            // 根据策略执行限流检查
            RateLimitResult result = executeRateLimitCheck(rateLimit, fullKey, context);
            
            // 记录监控数据
            if (properties.getEnableMonitor()) {
                monitor.recordRateLimitEvent(
                    fullKey, 
                    result.isAllowed(), 
                    result.getRemaining(), 
                    result.getLimit(),
                    rateLimit.strategy().getName(),
                    result.isHotspot(),
                    getClientIp(context)
                );
            }
            
            // 记录日志
            if (properties.getEnableLog() && rateLimit.enableLog()) {
                logRateLimitResult(fullKey, result, rateLimit);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Rate limit check failed for key: {}", fullKey, e);
            
            // 出现异常时，根据配置决定是否允许通过
            if (rateLimit.ignoreException()) {
                return RateLimitResult.allowed(rateLimit.limit(), rateLimit.limit(),
                                             System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(rateLimit.period()));
            } else {
                return RateLimitResult.denied(rateLimit.limit(),
                                            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(rateLimit.period()),
                                            "限流检查失败：" + e.getMessage());
            }
        }
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitStrategy strategy, RateLimitType type,
                                    long limit, long period, Map<String, Object> context) {
        // 检查是否启用限流
        if (!properties.getEnabled()) {
            return RateLimitResult.allowed(limit, limit, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(period));
        }

        // 构建完整的限流key
        String fullKey = buildRateLimitKey(key, type, context);
        
        // 记录活跃key
        activeKeys.add(fullKey);

        try {
            // 根据策略执行限流检查
            RateLimitResult result = executeRateLimitCheck(strategy, fullKey, limit, period, context);
            
            // 记录监控数据
            if (properties.getEnableMonitor()) {
                monitor.recordRateLimitEvent(
                    fullKey, 
                    result.isAllowed(), 
                    result.getRemaining(), 
                    result.getLimit(),
                    strategy.getName(),
                    result.isHotspot(),
                    getClientIp(context)
                );
            }
            
            // 记录日志
            if (properties.getEnableLog()) {
                logRateLimitResult(fullKey, result, strategy, type);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Rate limit check failed for key: {}", fullKey, e);
            return RateLimitResult.denied(limit, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(period),
                                        "限流检查失败：" + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<RateLimitResult> tryAcquireAsync(RateLimit rateLimit, String key, 
                                                             Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> tryAcquire(rateLimit, key, context));
    }

    @Override
    public Map<String, RateLimitResult> tryAcquireBatch(Map<String, BatchRequest> requests) {
        Map<String, RateLimitResult> results = new HashMap<>();
        
        // 串行处理批量请求（确保Redis原子性）
        for (Map.Entry<String, BatchRequest> entry : requests.entrySet()) {
            String requestKey = entry.getKey();
            BatchRequest request = entry.getValue();
            
            RateLimitResult result = tryAcquire(request.getRateLimit(), request.getKey(), request.getContext());
            results.put(requestKey, result);
        }
        
        return results;
    }

    /**
     * 执行限流检查
     */
    private RateLimitResult executeRateLimitCheck(RateLimit rateLimit, String key, Map<String, Object> context) {
        RateLimitStrategy strategy = rateLimit.strategy();
        long limit = rateLimit.limit();
        long period = TimeUnit.SECONDS.convert(rateLimit.period(), rateLimit.timeUnit());
        
        return executeRateLimitCheck(strategy, key, limit, period, context, rateLimit);
    }

    /**
     * 执行限流检查（通用方法）
     */
    private RateLimitResult executeRateLimitCheck(RateLimitStrategy strategy, String key, long limit, long period,
                                                Map<String, Object> context) {
        return executeRateLimitCheck(strategy, key, limit, period, context, null);
    }

    /**
     * 执行限流检查（完整版本）
     */
    private RateLimitResult executeRateLimitCheck(RateLimitStrategy strategy, String key, long limit, long period,
                                                Map<String, Object> context, RateLimit rateLimit) {
        DefaultRedisScript<List> script = scriptCache.get(strategy);
        if (script == null) {
            throw new IllegalArgumentException("Unsupported rate limit strategy: " + strategy);
        }

        List<String> keys = Collections.singletonList(key);
        List<String> args = buildScriptArgs(strategy, limit, period, context, rateLimit);

        // 执行Lua脚本
        List<Object> result = redisTemplate.execute(script, keys, args.toArray());
        
        if (result == null || result.size() < 4) {
            throw new RuntimeException("Invalid script result: " + result);
        }

        // 解析脚本结果
        return parseScriptResult(result, strategy, key, limit);
    }

    /**
     * 构建脚本参数
     */
    private List<String> buildScriptArgs(RateLimitStrategy strategy, long limit, long period,
                                       Map<String, Object> context, RateLimit rateLimit) {
        List<String> args = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        switch (strategy) {
            case SLIDING_WINDOW:
                args.add(String.valueOf(period));           // 窗口大小
                args.add(String.valueOf(limit));            // 限流阈值
                args.add(String.valueOf(currentTime));      // 当前时间戳
                args.add(String.valueOf(getWindowSlices(rateLimit))); // 窗口分片数
                break;

            case TOKEN_BUCKET:
                args.add(String.valueOf(getBucketCapacity(rateLimit, limit)));  // 桶容量
                args.add(String.valueOf(getRefillRate(rateLimit, limit, period))); // 令牌生成速率
                args.add(String.valueOf(currentTime));      // 当前时间戳
                args.add("1");                              // 请求令牌数量
                args.add(String.valueOf(getWarmupPeriod(rateLimit))); // 预热时间
                break;

            case LEAKY_BUCKET:
                args.add(String.valueOf(getLeakyBucketCapacity(rateLimit, limit))); // 桶容量
                args.add(String.valueOf(getLeakRate(rateLimit, limit, period)));    // 漏出速率
                args.add(String.valueOf(currentTime));      // 当前时间戳
                args.add("1");                              // 请求数量
                break;

            case FIXED_WINDOW:
                args.add(String.valueOf(period));           // 窗口大小
                args.add(String.valueOf(limit));            // 限流阈值
                args.add(String.valueOf(currentTime));      // 当前时间戳
                args.add("1");                              // 请求数量
                break;

            case COUNTER:
                args.add(String.valueOf(period));           // 时间窗口
                args.add(String.valueOf(limit));            // 限流阈值
                args.add(String.valueOf(currentTime));      // 当前时间戳
                args.add("1");                              // 请求数量
                break;

            case DISTRIBUTED_TOKEN_BUCKET:
                args.add(String.valueOf(getBucketCapacity(rateLimit, limit)));  // 桶容量
                args.add(String.valueOf(getRefillRate(rateLimit, limit, period))); // 令牌生成速率
                args.add(String.valueOf(currentTime));      // 当前时间戳
                args.add(getNodeId(context));               // 节点ID
                args.add("1");                              // 请求令牌数量
                args.add("1.0");                            // 节点权重
                break;

            case HOTSPOT:
                String paramValue = getHotspotParameter(context);
                args.add(paramValue);                       // 参数值
                args.add(String.valueOf(limit));           // 普通限流阈值
                args.add(String.valueOf(limit / 5));       // 热点限流阈值
                args.add(String.valueOf(period));          // 时间窗口
                args.add(String.valueOf(currentTime));     // 当前时间戳
                args.add(String.valueOf(limit * 2));       // 热点检测阈值
                args.add("300");                           // 热点检测窗口
                break;

            default:
                throw new IllegalArgumentException("Unsupported strategy for script args: " + strategy);
        }

        return args;
    }

    /**
     * 解析脚本执行结果
     */
    private RateLimitResult parseScriptResult(List<Object> result, RateLimitStrategy strategy, String key, long limit) {
        // 通用结果解析: [是否允许, 剩余配额/令牌数, 重置时间/下次更新时间, 当前计数/等待时间]
        int allowed = ((Number) result.get(0)).intValue();
        long remaining = ((Number) result.get(1)).longValue();
        long resetTime = ((Number) result.get(2)).longValue();
        long current = result.size() > 3 ? ((Number) result.get(3)).longValue() : 0;
        
        RateLimitResult.RateLimitResultBuilder builder = RateLimitResult.builder()
                .allowed(allowed == 1)
                .remaining(remaining)
                .limit(limit)
                .current(current)
                .resetTime(resetTime)
                .resetDateTime(LocalDateTime.now().plusSeconds((resetTime - System.currentTimeMillis()) / 1000))
                .key(key)
                .strategy(strategy.getName())
                .checkTime(LocalDateTime.now());

        // 特殊处理热点数据结果
        if (strategy == RateLimitStrategy.HOTSPOT && result.size() >= 4) {
            int isHotspot = ((Number) result.get(1)).intValue(); // 第二个参数是是否热点
            remaining = ((Number) result.get(2)).longValue();     // 第三个参数是剩余配额
            int hotspotLevel = result.size() > 3 ? ((Number) result.get(3)).intValue() : 0;
            
            builder.hotspot(isHotspot == 1)
                   .hotspotLevel(hotspotLevel)
                   .remaining(remaining);
        }

        // 设置等待时间（仅对某些策略有效）
        if (!builder.build().isAllowed()) {
            switch (strategy) {
                case TOKEN_BUCKET:
                case LEAKY_BUCKET:
                    builder.waitTime(current); // 对这些策略，第四个参数是等待时间
                    break;
                default:
                    builder.waitTime(Math.max(0, resetTime - System.currentTimeMillis()));
                    break;
            }
        }

        RateLimitResult finalResult = builder.build();
        
        // 设置错误信息
        if (!finalResult.isAllowed()) {
            String message = finalResult.isHotspot() ? 
                "热点数据访问过于频繁，请稍后再试" : "请求过于频繁，请稍后再试";
            finalResult.setMessage(message);
            finalResult.setErrorCode(429);
        }

        return finalResult;
    }

    /**
     * 构建限流key
     */
    private String buildRateLimitKey(RateLimit rateLimit, String key, Map<String, Object> context) {
        StringBuilder keyBuilder = new StringBuilder(properties.getKeyPrefix()).append(":");
        
        // 添加类型前缀
        keyBuilder.append(rateLimit.type().getCode()).append(":");
        
        // 添加策略前缀
        keyBuilder.append(rateLimit.strategy().getCode()).append(":");
        
        // 根据类型构建具体的key
        switch (rateLimit.type()) {
            case IP:
                String ip = getClientIp(context);
                keyBuilder.append("ip:").append(ip);
                break;
            case USER:
                String userId = getUserId(context);
                keyBuilder.append("user:").append(userId);
                break;
            case API:
                String api = getApiPath(context);
                keyBuilder.append("api:").append(api.replace("/", "_"));
                break;
            case CUSTOM:
                keyBuilder.append("custom:").append(key);
                break;
            default:
                keyBuilder.append("global:").append(key);
                break;
        }
        
        return keyBuilder.toString();
    }

    /**
     * 构建限流key（简化版本）
     */
    private String buildRateLimitKey(String key, RateLimitType type, Map<String, Object> context) {
        return properties.getKeyPrefix() + ":" + type.getCode() + ":" + key;
    }

    // 辅助方法：获取各种配置参数
    private int getWindowSlices(RateLimit rateLimit) {
        return rateLimit != null && rateLimit.windowSlices() > 0 ? 
               rateLimit.windowSlices() : 
               properties.getStrategy().getSlidingWindow().getDefaultSlices();
    }

    private long getBucketCapacity(RateLimit rateLimit, long defaultCapacity) {
        return rateLimit != null && rateLimit.bucketCapacity() > 0 ? 
               rateLimit.bucketCapacity() : defaultCapacity;
    }

    private double getRefillRate(RateLimit rateLimit, long limit, long period) {
        if (rateLimit != null && rateLimit.refillRate() > 0) {
            return rateLimit.refillRate();
        }
        return (double) limit / period; // 默认按期间内均匀生成
    }

    private long getWarmupPeriod(RateLimit rateLimit) {
        return rateLimit != null ? rateLimit.warmupPeriod() : 0;
    }

    private long getLeakyBucketCapacity(RateLimit rateLimit, long defaultCapacity) {
        return rateLimit != null && rateLimit.leakyBucketCapacity() > 0 ? 
               rateLimit.leakyBucketCapacity() : defaultCapacity;
    }

    private double getLeakRate(RateLimit rateLimit, long limit, long period) {
        if (rateLimit != null && rateLimit.leakRate() > 0) {
            return rateLimit.leakRate();
        }
        return (double) limit / period; // 默认按期间内均匀漏出
    }

    private String getNodeId(Map<String, Object> context) {
        Object nodeId = context != null ? context.get("nodeId") : null;
        return nodeId != null ? nodeId.toString() : "default";
    }

    private String getHotspotParameter(Map<String, Object> context) {
        if (context == null) return "";
        
        // 尝试获取热点参数
        Object param = context.get("hotspotParam");
        if (param != null) {
            return param.toString();
        }
        
        // 尝试获取用户ID作为热点参数
        Object userId = context.get("userId");
        if (userId != null) {
            return userId.toString();
        }
        
        return "";
    }

    private String getClientIp(Map<String, Object> context) {
        if (context == null) return "unknown";
        Object ip = context.get("clientIp");
        return ip != null ? ip.toString() : "unknown";
    }

    private String getUserId(Map<String, Object> context) {
        if (context == null) return "anonymous";
        Object userId = context.get("userId");
        return userId != null ? userId.toString() : "anonymous";
    }

    private String getApiPath(Map<String, Object> context) {
        if (context == null) return "unknown";
        Object path = context.get("apiPath");
        return path != null ? path.toString() : "unknown";
    }

    /**
     * 记录限流结果日志
     */
    private void logRateLimitResult(String key, RateLimitResult result, RateLimit rateLimit) {
        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded - Key: {}, Strategy: {}, Type: {}, Remaining: {}/{}, ResetIn: {}s",
                    key, rateLimit.strategy().getName(), rateLimit.type().getName(),
                    result.getRemaining(), result.getLimit(), result.getSecondsToReset());
        } else {
            log.debug("Rate limit passed - Key: {}, Remaining: {}/{}", 
                     key, result.getRemaining(), result.getLimit());
        }
    }

    private void logRateLimitResult(String key, RateLimitResult result, RateLimitStrategy strategy, RateLimitType type) {
        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded - Key: {}, Strategy: {}, Type: {}, Remaining: {}/{}, ResetIn: {}s",
                    key, strategy.getName(), type.getName(),
                    result.getRemaining(), result.getLimit(), result.getSecondsToReset());
        }
    }

    @Override
    public boolean reset(String key) {
        try {
            // 删除所有相关的key
            Set<String> keysToDelete = redisTemplate.keys(properties.getKeyPrefix() + ":*:" + key + "*");
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                activeKeys.removeAll(keysToDelete);
                log.info("Reset rate limiter for key: {}, deleted {} keys", key, keysToDelete.size());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to reset rate limiter for key: {}", key, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getStatistics(String key) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取所有相关的key
            Set<String> relatedKeys = redisTemplate.keys(properties.getKeyPrefix() + ":*:" + key + "*");
            
            stats.put("relatedKeys", relatedKeys != null ? relatedKeys.size() : 0);
            stats.put("keyPattern", properties.getKeyPrefix() + ":*:" + key + "*");
            stats.put("timestamp", System.currentTimeMillis());
            
            // 如果有相关key，获取更详细的信息
            if (relatedKeys != null && !relatedKeys.isEmpty()) {
                for (String relatedKey : relatedKeys) {
                    Long ttl = redisTemplate.getExpire(relatedKey);
                    stats.put(relatedKey + "_ttl", ttl);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to get statistics for key: {}", key, e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    @Override
    public boolean warmup(String key, RateLimitStrategy strategy, Map<String, Object> params) {
        // 预热主要针对令牌桶策略
        if (strategy != RateLimitStrategy.TOKEN_BUCKET && strategy != RateLimitStrategy.DISTRIBUTED_TOKEN_BUCKET) {
            return false;
        }
        
        try {
            // 为令牌桶预填充令牌
            String bucketKey = properties.getKeyPrefix() + ":" + strategy.getCode() + ":" + key + ":token_bucket:tokens";
            Object capacity = params.get("capacity");
            
            if (capacity != null) {
                redisTemplate.opsForValue().set(bucketKey, capacity.toString());
                log.info("Warmed up token bucket for key: {} with capacity: {}", key, capacity);
                return true;
            }
            
        } catch (Exception e) {
            log.error("Failed to warmup rate limiter for key: {}", key, e);
        }
        
        return false;
    }

    @Override
    public boolean updateConfig(String key, long newLimit, long newPeriod) {
        // Redis-based implementation doesn't need explicit config update
        // The configuration is passed with each request
        log.info("Rate limit config updated for key: {}, newLimit: {}, newPeriod: {}", key, newLimit, newPeriod);
        return true;
    }

    @Override
    public boolean exists(String key) {
        try {
            Set<String> keys = redisTemplate.keys(properties.getKeyPrefix() + ":*:" + key + "*");
            return keys != null && !keys.isEmpty();
        } catch (Exception e) {
            log.error("Failed to check existence of key: {}", key, e);
            return false;
        }
    }

    @Override
    public boolean delete(String key) {
        return reset(key); // delete is same as reset for Redis implementation
    }

    @Override
    public Set<String> getActiveKeys() {
        return new HashSet<>(activeKeys);
    }
}
package com.musicserver.ratelimit.service;

import com.musicserver.ratelimit.annotation.RateLimit;
import com.musicserver.ratelimit.dto.RateLimitResult;
import com.musicserver.ratelimit.enums.RateLimitStrategy;
import com.musicserver.ratelimit.enums.RateLimitType;

import java.util.Map;

/**
 * 限流服务接口
 * 
 * 定义了限流服务的核心方法
 * 支持多种限流策略和维度的限流检查
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public interface RateLimitService {

    /**
     * 检查是否允许通过（基于注解配置）
     * 
     * @param rateLimit 限流注解
     * @param key 限流key
     * @param context 上下文信息
     * @return 限流结果
     */
    RateLimitResult tryAcquire(RateLimit rateLimit, String key, Map<String, Object> context);

    /**
     * 检查是否允许通过（基于参数配置）
     * 
     * @param key 限流key
     * @param strategy 限流策略
     * @param type 限流类型
     * @param limit 限流阈值
     * @param period 时间窗口
     * @param context 上下文信息
     * @return 限流结果
     */
    RateLimitResult tryAcquire(String key, RateLimitStrategy strategy, RateLimitType type, 
                              long limit, long period, Map<String, Object> context);

    /**
     * 异步限流检查
     * 
     * @param rateLimit 限流注解
     * @param key 限流key
     * @param context 上下文信息
     * @return 限流结果的异步Future
     */
    java.util.concurrent.CompletableFuture<RateLimitResult> tryAcquireAsync(
            RateLimit rateLimit, String key, Map<String, Object> context);

    /**
     * 批量限流检查
     * 
     * @param requests 批量请求参数
     * @return 批量限流结果
     */
    Map<String, RateLimitResult> tryAcquireBatch(Map<String, BatchRequest> requests);

    /**
     * 重置限流状态
     * 
     * @param key 限流key
     * @return 是否重置成功
     */
    boolean reset(String key);

    /**
     * 获取限流统计信息
     * 
     * @param key 限流key
     * @return 统计信息
     */
    Map<String, Object> getStatistics(String key);

    /**
     * 预热限流器（主要用于令牌桶策略）
     * 
     * @param key 限流key
     * @param strategy 限流策略
     * @param params 预热参数
     * @return 是否预热成功
     */
    boolean warmup(String key, RateLimitStrategy strategy, Map<String, Object> params);

    /**
     * 动态更新限流配置
     * 
     * @param key 限流key
     * @param newLimit 新的限流阈值
     * @param newPeriod 新的时间窗口
     * @return 是否更新成功
     */
    boolean updateConfig(String key, long newLimit, long newPeriod);

    /**
     * 检查限流器是否存在
     * 
     * @param key 限流key
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 删除限流器
     * 
     * @param key 限流key
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 获取所有活跃的限流器key
     * 
     * @return 限流器key列表
     */
    java.util.Set<String> getActiveKeys();

    /**
     * 批量请求参数类
     */
    class BatchRequest {
        private RateLimit rateLimit;
        private String key;
        private Map<String, Object> context;

        public BatchRequest(RateLimit rateLimit, String key, Map<String, Object> context) {
            this.rateLimit = rateLimit;
            this.key = key;
            this.context = context;
        }

        // Getters
        public RateLimit getRateLimit() { return rateLimit; }
        public String getKey() { return key; }
        public Map<String, Object> getContext() { return context; }
    }
}
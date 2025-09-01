package com.musicserver.ratelimit.service.impl;

import com.musicserver.ratelimit.annotation.RateLimit;
import com.musicserver.ratelimit.config.RateLimitProperties;
import com.musicserver.ratelimit.dto.RateLimitResult;
import com.musicserver.ratelimit.enums.RateLimitStrategy;
import com.musicserver.ratelimit.enums.RateLimitType;
import com.musicserver.ratelimit.monitor.RateLimitMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis限流服务单元测试
 * 
 * 测试RedisRateLimitService的各种限流策略和功能
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@ExtendWith(MockitoExtension.class)
class RedisRateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RateLimitMonitor monitor;

    private RateLimitProperties properties;
    private RedisRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setEnableLog(true);
        properties.setEnableMonitor(true);
        properties.setKeyPrefix("test_rate_limit");
        
        rateLimitService = new RedisRateLimitService(redisTemplate, properties, monitor);
    }

    @Test
    void testTryAcquireWithAnnotation_Allowed() {
        // 准备测试数据
        RateLimit rateLimit = createMockRateLimit(
            100L, 60L, TimeUnit.SECONDS,
            RateLimitStrategy.SLIDING_WINDOW,
            RateLimitType.GLOBAL
        );

        Map<String, Object> context = new HashMap<>();
        context.put("clientIp", "127.0.0.1");

        // 模拟Redis脚本执行结果：允许通过
        List<Object> scriptResult = Arrays.asList(1, 99L, System.currentTimeMillis() + 60000L, 1L);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
            .thenReturn(scriptResult);

        // 执行测试
        RateLimitResult result = rateLimitService.tryAcquire(rateLimit, "test_key", context);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getRemaining()).isEqualTo(99L);
        assertThat(result.getLimit()).isEqualTo(100L);

        // 验证监控记录被调用
        verify(monitor).recordRateLimitEvent(
            anyString(), eq(true), eq(99L), eq(100L),
            eq("滑动窗口算法"), eq(false), eq("127.0.0.1")
        );
    }

    @Test
    void testTryAcquireWithAnnotation_Denied() {
        // 准备测试数据
        RateLimit rateLimit = createMockRateLimit(
            10L, 60L, TimeUnit.SECONDS,
            RateLimitStrategy.FIXED_WINDOW,
            RateLimitType.IP
        );

        Map<String, Object> context = new HashMap<>();
        context.put("clientIp", "192.168.1.100");

        // 模拟Redis脚本执行结果：拒绝通过
        List<Object> scriptResult = Arrays.asList(0, 0L, System.currentTimeMillis() + 30000L, 10L);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
            .thenReturn(scriptResult);

        // 执行测试
        RateLimitResult result = rateLimitService.tryAcquire(rateLimit, "test_key", context);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getRemaining()).isEqualTo(0L);
        assertThat(result.getLimit()).isEqualTo(10L);
        assertThat(result.getCurrent()).isEqualTo(10L);

        // 验证监控记录被调用
        verify(monitor).recordRateLimitEvent(
            anyString(), eq(false), eq(0L), eq(10L),
            eq("固定窗口算法"), eq(false), eq("192.168.1.100")
        );
    }

    @Test
    void testTryAcquireWithParameters() {
        // 准备测试数据
        String key = "api_test";
        RateLimitStrategy strategy = RateLimitStrategy.TOKEN_BUCKET;
        RateLimitType type = RateLimitType.API;
        long limit = 50L;
        long period = 30L;
        Map<String, Object> context = new HashMap<>();

        // 模拟Redis脚本执行结果
        List<Object> scriptResult = Arrays.asList(1, 25L, System.currentTimeMillis() + 10000L, 0L);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
            .thenReturn(scriptResult);

        // 执行测试
        RateLimitResult result = rateLimitService.tryAcquire(key, strategy, type, limit, period, context);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getRemaining()).isEqualTo(25L);
        assertThat(result.getLimit()).isEqualTo(50L);
    }

    @Test
    void testTryAcquireAsync() throws Exception {
        // 准备测试数据
        RateLimit rateLimit = createMockRateLimit(
            100L, 60L, TimeUnit.SECONDS,
            RateLimitStrategy.SLIDING_WINDOW,
            RateLimitType.GLOBAL
        );

        Map<String, Object> context = new HashMap<>();

        // 模拟Redis脚本执行结果
        List<Object> scriptResult = Arrays.asList(1, 99L, System.currentTimeMillis() + 60000L, 1L);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
            .thenReturn(scriptResult);

        // 执行异步测试
        var future = rateLimitService.tryAcquireAsync(rateLimit, "test_key", context);
        RateLimitResult result = future.get();

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isAllowed()).isTrue();
    }

    @Test
    void testTryAcquireBatch() {
        // 准备批量请求
        Map<String, RateLimitService.BatchRequest> requests = new HashMap<>();
        
        RateLimit rateLimit1 = createMockRateLimit(100L, 60L, TimeUnit.SECONDS, 
                                                  RateLimitStrategy.SLIDING_WINDOW, RateLimitType.GLOBAL);
        RateLimit rateLimit2 = createMockRateLimit(50L, 30L, TimeUnit.SECONDS,
                                                  RateLimitStrategy.TOKEN_BUCKET, RateLimitType.USER);
        
        requests.put("req1", new RateLimitService.BatchRequest(rateLimit1, "key1", new HashMap<>()));
        requests.put("req2", new RateLimitService.BatchRequest(rateLimit2, "key2", new HashMap<>()));

        // 模拟Redis脚本执行结果
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
            .thenReturn(Arrays.asList(1, 99L, System.currentTimeMillis() + 60000L, 1L))
            .thenReturn(Arrays.asList(0, 0L, System.currentTimeMillis() + 30000L, 50L));

        // 执行批量测试
        Map<String, RateLimitResult> results = rateLimitService.tryAcquireBatch(requests);

        // 验证结果
        assertThat(results).hasSize(2);
        assertThat(results.get("req1").isAllowed()).isTrue();
        assertThat(results.get("req2").isAllowed()).isFalse();
    }

    @Test
    void testReset() {
        // 模拟Redis keys操作
        Set<String> keysToDelete = Set.of("rate_limit:key1", "rate_limit:key2");
        when(redisTemplate.keys(anyString())).thenReturn(keysToDelete);

        // 执行重置测试
        boolean result = rateLimitService.reset("test_key");

        // 验证结果
        assertThat(result).isTrue();
        verify(redisTemplate).delete(keysToDelete);
    }

    @Test
    void testGetStatistics() {
        // 模拟Redis keys操作
        Set<String> relatedKeys = Set.of("rate_limit:key1", "rate_limit:key2");
        when(redisTemplate.keys(anyString())).thenReturn(relatedKeys);
        when(redisTemplate.getExpire("rate_limit:key1")).thenReturn(3600L);
        when(redisTemplate.getExpire("rate_limit:key2")).thenReturn(1800L);

        // 执行统计测试
        Map<String, Object> stats = rateLimitService.getStatistics("test_key");

        // 验证结果
        assertThat(stats).isNotEmpty();
        assertThat(stats.get("relatedKeys")).isEqualTo(2);
        assertThat(stats).containsKey("timestamp");
    }

    @Test
    void testWarmup() {
        // 准备预热参数
        Map<String, Object> params = new HashMap<>();
        params.put("capacity", 100L);

        // 执行预热测试
        boolean result = rateLimitService.warmup("test_key", RateLimitStrategy.TOKEN_BUCKET, params);

        // 验证结果
        assertThat(result).isTrue();
        verify(redisTemplate.opsForValue()).set(anyString(), eq("100"));
    }

    @Test
    void testUpdateConfig() {
        // 执行配置更新测试
        boolean result = rateLimitService.updateConfig("test_key", 200L, 120L);

        // 验证结果 - 对于Redis实现，这个方法总是返回true
        assertThat(result).isTrue();
    }

    @Test
    void testExists() {
        // 模拟Redis keys操作
        Set<String> existingKeys = Set.of("rate_limit:test_key");
        when(redisTemplate.keys(anyString())).thenReturn(existingKeys);

        // 执行存在性检查
        boolean result = rateLimitService.exists("test_key");

        // 验证结果
        assertThat(result).isTrue();
    }

    @Test
    void testDelete() {
        // 模拟Redis keys操作
        Set<String> keysToDelete = Set.of("rate_limit:test_key");
        when(redisTemplate.keys(anyString())).thenReturn(keysToDelete);

        // 执行删除测试
        boolean result = rateLimitService.delete("test_key");

        // 验证结果
        assertThat(result).isTrue();
        verify(redisTemplate).delete(keysToDelete);
    }

    @Test
    void testGetActiveKeys() {
        // 这个方法返回内存中的活跃keys
        Set<String> activeKeys = rateLimitService.getActiveKeys();

        // 验证结果 - 初始状态应该为空
        assertThat(activeKeys).isNotNull();
    }

    @Test
    void testServiceDisabled() {
        // 禁用限流服务
        properties.setEnabled(false);

        RateLimit rateLimit = createMockRateLimit(10L, 60L, TimeUnit.SECONDS,
                                                 RateLimitStrategy.SLIDING_WINDOW, RateLimitType.GLOBAL);

        // 执行测试
        RateLimitResult result = rateLimitService.tryAcquire(rateLimit, "test_key", new HashMap<>());

        // 验证结果 - 应该总是允许通过
        assertThat(result.isAllowed()).isTrue();

        // 验证Redis脚本没有被执行
        verify(redisTemplate, never()).execute(any(DefaultRedisScript.class), anyList(), any());
    }

    @Test
    void testAnnotationDisabled() {
        // 准备禁用的注解
        RateLimit rateLimit = createMockRateLimit(10L, 60L, TimeUnit.SECONDS,
                                                 RateLimitStrategy.SLIDING_WINDOW, RateLimitType.GLOBAL);
        when(rateLimit.enabled()).thenReturn(false);

        // 执行测试
        RateLimitResult result = rateLimitService.tryAcquire(rateLimit, "test_key", new HashMap<>());

        // 验证结果 - 应该总是允许通过
        assertThat(result.isAllowed()).isTrue();

        // 验证Redis脚本没有被执行
        verify(redisTemplate, never()).execute(any(DefaultRedisScript.class), anyList(), any());
    }

    /**
     * 创建模拟的RateLimit注解
     */
    private RateLimit createMockRateLimit(long limit, long period, TimeUnit timeUnit,
                                         RateLimitStrategy strategy, RateLimitType type) {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(limit);
        when(rateLimit.period()).thenReturn(period);
        when(rateLimit.timeUnit()).thenReturn(timeUnit);
        when(rateLimit.strategy()).thenReturn(strategy);
        when(rateLimit.type()).thenReturn(type);
        when(rateLimit.enabled()).thenReturn(true);
        when(rateLimit.enableLog()).thenReturn(true);
        when(rateLimit.message()).thenReturn("请求过于频繁，请稍后再试");
        when(rateLimit.errorCode()).thenReturn(429);
        when(rateLimit.windowSlices()).thenReturn(0);
        when(rateLimit.bucketCapacity()).thenReturn(0L);
        when(rateLimit.refillRate()).thenReturn(0.0);
        when(rateLimit.warmupPeriod()).thenReturn(0L);
        when(rateLimit.leakyBucketCapacity()).thenReturn(0L);
        when(rateLimit.leakRate()).thenReturn(0.0);
        return rateLimit;
    }
}
package com.musicserver.ratelimit.config;

import com.musicserver.ratelimit.enums.RateLimitStrategy;
import com.musicserver.ratelimit.enums.RateLimitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 限流配置属性单元测试
 * 
 * 测试RateLimitProperties的各种配置功能
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
class RateLimitPropertiesTest {

    private RateLimitProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
    }

    @Test
    void testDefaultValues() {
        assertThat(properties.getEnabled()).isTrue();
        assertThat(properties.getDefaultStrategy()).isEqualTo(RateLimitStrategy.SLIDING_WINDOW);
        assertThat(properties.getDefaultType()).isEqualTo(RateLimitType.GLOBAL);
        assertThat(properties.getDefaultLimit()).isEqualTo(100L);
        assertThat(properties.getDefaultPeriod()).isEqualTo(60L);
        assertThat(properties.getDefaultTimeUnit()).isEqualTo(TimeUnit.SECONDS);
        assertThat(properties.getEnableLog()).isTrue();
        assertThat(properties.getEnableMonitor()).isTrue();
        assertThat(properties.getKeyPrefix()).isEqualTo("rate_limit");
    }

    @Test
    void testRedisConfig() {
        RateLimitProperties.RedisConfig redisConfig = properties.getRedis();
        
        assertThat(redisConfig).isNotNull();
        assertThat(redisConfig.getExpireTime()).isEqualTo(7200L);
        assertThat(redisConfig.getScriptCacheTime()).isEqualTo(3600L);
        assertThat(redisConfig.getConnectionTimeout()).isEqualTo(5000L);
        assertThat(redisConfig.getCommandTimeout()).isEqualTo(3000L);
        assertThat(redisConfig.getEnablePipeline()).isFalse();
        assertThat(redisConfig.getPipelineBatchSize()).isEqualTo(100);
    }

    @Test
    void testGlobalConfig() {
        RateLimitProperties.GlobalConfig globalConfig = properties.getGlobal();
        
        assertThat(globalConfig).isNotNull();
        assertThat(globalConfig.getEnabled()).isTrue();
        assertThat(globalConfig.getLimit()).isEqualTo(10000L);
        assertThat(globalConfig.getPeriod()).isEqualTo(60L);
        assertThat(globalConfig.getStrategy()).isEqualTo(RateLimitStrategy.SLIDING_WINDOW);
    }

    @Test
    void testIpConfig() {
        RateLimitProperties.IpConfig ipConfig = properties.getIp();
        
        assertThat(ipConfig).isNotNull();
        assertThat(ipConfig.getEnabled()).isTrue();
        assertThat(ipConfig.getLimit()).isEqualTo(1000L);
        assertThat(ipConfig.getPeriod()).isEqualTo(60L);
        assertThat(ipConfig.getStrategy()).isEqualTo(RateLimitStrategy.SLIDING_WINDOW);
        assertThat(ipConfig.getWhitelist()).contains("127.0.0.1", "::1");
        assertThat(ipConfig.getBlacklist()).isEmpty();
        assertThat(ipConfig.getEnableGeoLimit()).isFalse();
    }

    @Test
    void testUserConfig() {
        RateLimitProperties.UserConfig userConfig = properties.getUser();
        
        assertThat(userConfig).isNotNull();
        assertThat(userConfig.getEnabled()).isTrue();
        assertThat(userConfig.getLimit()).isEqualTo(500L);
        assertThat(userConfig.getPeriod()).isEqualTo(60L);
        assertThat(userConfig.getStrategy()).isEqualTo(RateLimitStrategy.TOKEN_BUCKET);
        assertThat(userConfig.getRoles()).isNotNull();
    }

    @Test
    void testApiConfig() {
        RateLimitProperties.ApiConfig apiConfig = properties.getApi();
        
        assertThat(apiConfig).isNotNull();
        assertThat(apiConfig.getEnabled()).isTrue();
        assertThat(apiConfig.getLimit()).isEqualTo(200L);
        assertThat(apiConfig.getPeriod()).isEqualTo(60L);
        assertThat(apiConfig.getStrategy()).isEqualTo(RateLimitStrategy.FIXED_WINDOW);
        assertThat(apiConfig.getSpecificApis()).isNotNull();
    }

    @Test
    void testHotspotConfig() {
        RateLimitProperties.HotspotConfig hotspotConfig = properties.getHotspot();
        
        assertThat(hotspotConfig).isNotNull();
        assertThat(hotspotConfig.getEnabled()).isFalse();
        assertThat(hotspotConfig.getDetectionThreshold()).isEqualTo(200L);
        assertThat(hotspotConfig.getDetectionWindow()).isEqualTo(300L);
        assertThat(hotspotConfig.getHotspotLimit()).isEqualTo(50L);
        assertThat(hotspotConfig.getHotspotPeriod()).isEqualTo(60L);
        assertThat(hotspotConfig.getHotspotExpireTime()).isEqualTo(3600L);
        assertThat(hotspotConfig.getMaxHotspots()).isEqualTo(1000);
    }

    @Test
    void testSlidingWindowStrategyConfig() {
        RateLimitProperties.StrategyConfig.SlidingWindowConfig config = 
                properties.getStrategy().getSlidingWindow();
        
        assertThat(config).isNotNull();
        assertThat(config.getDefaultSlices()).isEqualTo(60);
        assertThat(config.getMaxSlices()).isEqualTo(3600);
        assertThat(config.getMinSlices()).isEqualTo(10);
    }

    @Test
    void testTokenBucketStrategyConfig() {
        RateLimitProperties.StrategyConfig.TokenBucketConfig config = 
                properties.getStrategy().getTokenBucket();
        
        assertThat(config).isNotNull();
        assertThat(config.getDefaultCapacity()).isEqualTo(100L);
        assertThat(config.getDefaultRefillRate()).isEqualTo(10.0);
        assertThat(config.getDefaultWarmupPeriod()).isEqualTo(0L);
        assertThat(config.getMaxCapacity()).isEqualTo(10000L);
        assertThat(config.getMaxRefillRate()).isEqualTo(1000.0);
    }

    @Test
    void testLeakyBucketStrategyConfig() {
        RateLimitProperties.StrategyConfig.LeakyBucketConfig config = 
                properties.getStrategy().getLeakyBucket();
        
        assertThat(config).isNotNull();
        assertThat(config.getDefaultCapacity()).isEqualTo(100L);
        assertThat(config.getDefaultLeakRate()).isEqualTo(10.0);
        assertThat(config.getMaxCapacity()).isEqualTo(10000L);
        assertThat(config.getMaxLeakRate()).isEqualTo(1000.0);
    }

    @Test
    void testFixedWindowStrategyConfig() {
        RateLimitProperties.StrategyConfig.FixedWindowConfig config = 
                properties.getStrategy().getFixedWindow();
        
        assertThat(config).isNotNull();
        assertThat(config.getDefaultWindowSize()).isEqualTo(60L);
        assertThat(config.getMaxWindowSize()).isEqualTo(3600L);
        assertThat(config.getMinWindowSize()).isEqualTo(1L);
    }

    @Test
    void testCounterStrategyConfig() {
        RateLimitProperties.StrategyConfig.CounterConfig config = 
                properties.getStrategy().getCounter();
        
        assertThat(config).isNotNull();
        assertThat(config.getDefaultResetInterval()).isEqualTo(60L);
        assertThat(config.getMaxResetInterval()).isEqualTo(86400L);
        assertThat(config.getMinResetInterval()).isEqualTo(1L);
    }

    @Test
    void testIsValid() {
        // 测试有效配置
        assertTrue(properties.isValid());
        
        // 测试无效配置
        properties.setEnabled(null);
        assertFalse(properties.isValid());
        
        properties.setEnabled(true);
        properties.setDefaultLimit(null);
        assertFalse(properties.isValid());
        
        properties.setDefaultLimit(-1L);
        assertFalse(properties.isValid());
        
        properties.setDefaultLimit(100L);
        properties.setDefaultPeriod(null);
        assertFalse(properties.isValid());
        
        properties.setDefaultPeriod(-1L);
        assertFalse(properties.isValid());
        
        properties.setDefaultPeriod(60L);
        properties.setKeyPrefix(null);
        assertFalse(properties.isValid());
        
        properties.setKeyPrefix("");
        assertFalse(properties.isValid());
        
        properties.setKeyPrefix("  ");
        assertFalse(properties.isValid());
    }

    @Test
    void testGetStrategyDefaults() {
        // 测试滑动窗口策略默认值
        Map<String, Object> slidingWindowDefaults = properties.getStrategyDefaults(RateLimitStrategy.SLIDING_WINDOW);
        assertThat(slidingWindowDefaults).containsEntry("slices", 60);
        assertThat(slidingWindowDefaults).containsEntry("maxSlices", 3600);
        assertThat(slidingWindowDefaults).containsEntry("minSlices", 10);

        // 测试令牌桶策略默认值
        Map<String, Object> tokenBucketDefaults = properties.getStrategyDefaults(RateLimitStrategy.TOKEN_BUCKET);
        assertThat(tokenBucketDefaults).containsEntry("capacity", 100L);
        assertThat(tokenBucketDefaults).containsEntry("refillRate", 10.0);
        assertThat(tokenBucketDefaults).containsEntry("warmupPeriod", 0L);

        // 测试漏桶策略默认值
        Map<String, Object> leakyBucketDefaults = properties.getStrategyDefaults(RateLimitStrategy.LEAKY_BUCKET);
        assertThat(leakyBucketDefaults).containsEntry("capacity", 100L);
        assertThat(leakyBucketDefaults).containsEntry("leakRate", 10.0);

        // 测试固定窗口策略默认值
        Map<String, Object> fixedWindowDefaults = properties.getStrategyDefaults(RateLimitStrategy.FIXED_WINDOW);
        assertThat(fixedWindowDefaults).containsEntry("windowSize", 60L);

        // 测试计数器策略默认值
        Map<String, Object> counterDefaults = properties.getStrategyDefaults(RateLimitStrategy.COUNTER);
        assertThat(counterDefaults).containsEntry("resetInterval", 60L);
    }

    @Test
    void testGetTypeConfig() {
        // 测试全局类型配置
        Map<String, Object> globalConfig = properties.getTypeConfig(RateLimitType.GLOBAL);
        assertThat(globalConfig).containsEntry("enabled", true);
        assertThat(globalConfig).containsEntry("limit", 10000L);
        assertThat(globalConfig).containsEntry("period", 60L);
        assertThat(globalConfig).containsEntry("strategy", RateLimitStrategy.SLIDING_WINDOW);

        // 测试IP类型配置
        Map<String, Object> ipConfig = properties.getTypeConfig(RateLimitType.IP);
        assertThat(ipConfig).containsEntry("enabled", true);
        assertThat(ipConfig).containsEntry("limit", 1000L);
        assertThat(ipConfig).containsEntry("period", 60L);
        assertThat(ipConfig).containsEntry("strategy", RateLimitStrategy.SLIDING_WINDOW);
        assertThat(ipConfig).containsKey("whitelist");
        assertThat(ipConfig).containsKey("blacklist");

        // 测试用户类型配置
        Map<String, Object> userConfig = properties.getTypeConfig(RateLimitType.USER);
        assertThat(userConfig).containsEntry("enabled", true);
        assertThat(userConfig).containsEntry("limit", 500L);
        assertThat(userConfig).containsEntry("period", 60L);
        assertThat(userConfig).containsEntry("strategy", RateLimitStrategy.TOKEN_BUCKET);

        // 测试API类型配置
        Map<String, Object> apiConfig = properties.getTypeConfig(RateLimitType.API);
        assertThat(apiConfig).containsEntry("enabled", true);
        assertThat(apiConfig).containsEntry("limit", 200L);
        assertThat(apiConfig).containsEntry("period", 60L);
        assertThat(apiConfig).containsEntry("strategy", RateLimitStrategy.FIXED_WINDOW);

        // 测试未知类型配置（使用默认值）
        Map<String, Object> unknownConfig = properties.getTypeConfig(RateLimitType.CUSTOM);
        assertThat(unknownConfig).containsEntry("enabled", true);
        assertThat(unknownConfig).containsEntry("limit", 100L);
        assertThat(unknownConfig).containsEntry("period", 60L);
        assertThat(unknownConfig).containsEntry("strategy", RateLimitStrategy.SLIDING_WINDOW);
    }

    @Test
    void testCustomRules() {
        // 添加自定义规则
        RateLimitProperties.CustomRuleConfig customRule = new RateLimitProperties.CustomRuleConfig();
        customRule.setName("API限流规则");
        customRule.setKeyExpression("#request.getRequestURI()");
        customRule.setLimit(100L);
        customRule.setPeriod(60L);
        customRule.setStrategy(RateLimitStrategy.SLIDING_WINDOW);
        customRule.setType(RateLimitType.API);
        customRule.setCondition("#request.getMethod() == 'POST'");
        customRule.setEnabled(true);
        customRule.setPriority(1);
        customRule.setDescription("针对POST请求的API限流");

        properties.getCustomRules().put("api_rule", customRule);

        // 验证自定义规则
        assertThat(properties.getCustomRules()).hasSize(1);
        assertThat(properties.getCustomRules().get("api_rule")).isEqualTo(customRule);
        assertThat(customRule.getName()).isEqualTo("API限流规则");
        assertThat(customRule.getLimit()).isEqualTo(100L);
        assertThat(customRule.getEnabled()).isTrue();
    }

    @Test
    void testRoleConfiguration() {
        // 配置角色限流
        RateLimitProperties.UserConfig.RoleConfig adminRole = new RateLimitProperties.UserConfig.RoleConfig();
        adminRole.setLimit(1000L);
        adminRole.setPeriod(60L);
        adminRole.setStrategy(RateLimitStrategy.TOKEN_BUCKET);

        RateLimitProperties.UserConfig.RoleConfig userRole = new RateLimitProperties.UserConfig.RoleConfig();
        userRole.setLimit(100L);
        userRole.setPeriod(60L);
        userRole.setStrategy(RateLimitStrategy.FIXED_WINDOW);

        properties.getUser().getRoles().put("ADMIN", adminRole);
        properties.getUser().getRoles().put("USER", userRole);

        // 验证角色配置
        assertThat(properties.getUser().getRoles()).hasSize(2);
        assertThat(properties.getUser().getRoles().get("ADMIN").getLimit()).isEqualTo(1000L);
        assertThat(properties.getUser().getRoles().get("USER").getLimit()).isEqualTo(100L);
    }

    @Test
    void testSpecificApiConfiguration() {
        // 配置特定API限流
        RateLimitProperties.ApiConfig.SpecificApiConfig loginApi = new RateLimitProperties.ApiConfig.SpecificApiConfig();
        loginApi.setPattern("/api/auth/login");
        loginApi.setLimit(10L);
        loginApi.setPeriod(300L); // 5分钟
        loginApi.setStrategy(RateLimitStrategy.FIXED_WINDOW);
        loginApi.setEnabled(true);

        properties.getApi().getSpecificApis().put("login", loginApi);

        // 验证特定API配置
        assertThat(properties.getApi().getSpecificApis()).hasSize(1);
        assertThat(properties.getApi().getSpecificApis().get("login").getLimit()).isEqualTo(10L);
        assertThat(properties.getApi().getSpecificApis().get("login").getPeriod()).isEqualTo(300L);
    }
}
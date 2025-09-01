package com.musicserver.ip.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.musicserver.ip.entity.IPLocation;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * IP定位服务配置类
 * 
 * 配置IP定位相关的Bean和组件
 * 支持ip2region、缓存、异步处理等
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(IpLocationProperties.class)
@ConditionalOnProperty(prefix = "ip-location", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IpLocationConfig {

    /**
     * 创建IP2Region搜索器Bean
     * 
     * @param properties IP定位配置属性
     * @return Searcher实例
     */
    @Bean
    public Searcher ip2regionSearcher(IpLocationProperties properties) {
        try {
            log.info("Initializing IP2Region searcher with database: {}", properties.getDatabasePath());
            
            // 从classpath加载ip2region数据库文件
            Resource resource = new ClassPathResource(properties.getDatabasePath());
            if (!resource.exists()) {
                throw new IllegalStateException("IP2Region database file not found: " + properties.getDatabasePath());
            }
            
            // 使用vectorIndex算法创建搜索器，性能最佳
            byte[] dbBinStr = resource.getInputStream().readAllBytes();
            Searcher searcher = Searcher.newWithBuffer(dbBinStr);
            
            log.info("IP2Region searcher initialized successfully");
            return searcher;
            
        } catch (IOException e) {
            log.error("Failed to initialize IP2Region searcher: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to load IP2Region database", e);
        }
    }

    /**
     * 创建本地缓存Bean
     * 
     * @param properties IP定位配置属性
     * @return 本地缓存实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "ip-location.cache", name = "enable-local-cache", havingValue = "true", matchIfMissing = true)
    public Cache<String, IPLocation> ipLocationLocalCache(IpLocationProperties properties) {
        log.info("Initializing IP location local cache");
        
        IpLocationProperties.CacheConfig cacheConfig = properties.getCache();
        
        return Caffeine.newBuilder()
                .maximumSize(cacheConfig.getLocalCacheMaxSize())
                .expireAfterWrite(Duration.ofMinutes(cacheConfig.getLocalCacheExpireMinutes()))
                .recordStats()
                .build();
    }

    /**
     * 创建IP统计异步执行器Bean
     * 
     * @param properties IP定位配置属性
     * @return 异步执行器
     */
    @Bean("ipStatisticsExecutor")
    @ConditionalOnProperty(prefix = "ip-location.statistics", name = "enable-async", havingValue = "true", matchIfMissing = true)
    public Executor ipStatisticsExecutor(IpLocationProperties properties) {
        log.info("Initializing IP statistics async executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getStatistics().getAsyncThreadPoolSize());
        executor.setMaxPoolSize(properties.getStatistics().getAsyncThreadPoolSize() * 2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("ip-stats-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        
        return executor;
    }

    /**
     * 创建IP查询异步执行器Bean
     * 
     * @param properties IP定位配置属性
     * @return 异步执行器
     */
    @Bean("ipQueryExecutor")
    @ConditionalOnProperty(prefix = "ip-location.performance", name = "enable-async-query", havingValue = "true")
    public Executor ipQueryExecutor(IpLocationProperties properties) {
        log.info("Initializing IP query async executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getPerformance().getAsyncThreadPoolSize());
        executor.setMaxPoolSize(properties.getPerformance().getAsyncThreadPoolSize() * 2);
        executor.setQueueCapacity(properties.getPerformance().getMaxConcurrentQueries());
        executor.setThreadNamePrefix("ip-query-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        
        return executor;
    }

    /**
     * 验证IP2Region数据库文件
     * 
     * @param properties IP定位配置属性
     */
    @Bean
    public IpLocationInitializer ipLocationInitializer(IpLocationProperties properties) {
        return new IpLocationInitializer(properties);
    }

    /**
     * IP定位初始化器
     */
    public static class IpLocationInitializer {
        
        private final IpLocationProperties properties;
        
        public IpLocationInitializer(IpLocationProperties properties) {
            this.properties = properties;
            validateConfiguration();
        }
        
        /**
         * 验证配置
         */
        private void validateConfiguration() {
            log.info("Validating IP location configuration");
            
            if (!properties.isValid()) {
                throw new IllegalStateException("Invalid IP location configuration");
            }
            
            // 验证数据库文件
            Resource resource = new ClassPathResource(properties.getDatabasePath());
            if (!resource.exists()) {
                log.warn("IP2Region database file not found at: {}", properties.getDatabasePath());
                log.info("Please ensure ip2region.xdb file is placed in classpath: {}", properties.getDatabasePath());
            }
            
            // 验证缓存配置
            if (properties.getEnableCache()) {
                log.info("IP location cache enabled - TTL: {}s, Max size: {}", 
                        properties.getCacheExpireSeconds(), 
                        properties.getCache().getMaxSize());
            }
            
            // 验证统计配置
            if (properties.getEnableStatistics()) {
                log.info("IP location statistics enabled - Retention: {} days, Async: {}", 
                        properties.getStatistics().getRetentionDays(),
                        properties.getStatistics().getEnableAsync());
            }
            
            // 验证安全配置
            if (properties.isSecurityEnabled()) {
                log.info("IP location security features enabled");
                if (properties.getSecurity().getEnableWhitelist()) {
                    log.info("IP whitelist enabled with {} entries", 
                            properties.getSecurity().getWhitelist().length);
                }
                if (properties.getSecurity().getEnableBlacklist()) {
                    log.info("IP blacklist enabled with {} entries", 
                            properties.getSecurity().getBlacklist().length);
                }
            }
            
            log.info("IP location configuration validation completed successfully");
        }
    }
}
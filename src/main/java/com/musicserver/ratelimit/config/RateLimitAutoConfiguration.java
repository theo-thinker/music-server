package com.musicserver.ratelimit.config;

import com.musicserver.ratelimit.aspect.RateLimitAspect;
import com.musicserver.ratelimit.monitor.RateLimitMonitor;
import com.musicserver.ratelimit.service.RateLimitService;
import com.musicserver.ratelimit.service.impl.RedisRateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 限流自动配置类
 * <p>
 * 自动配置限流相关的Bean和组件
 * 支持条件化配置和灵活的组件注册
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = "rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitAutoConfiguration {

    /**
     * 创建限流监控器Bean
     *
     * @param redisTemplate Redis模板
     * @param properties    限流配置属性
     * @return 限流监控器实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "rate-limit", name = "enable-monitor", havingValue = "true", matchIfMissing = true)
    public RateLimitMonitor rateLimitMonitor(StringRedisTemplate redisTemplate,
                                             RateLimitProperties properties) {
        log.info("Initializing Rate Limit Monitor");
        return new RateLimitMonitor(redisTemplate, properties);
    }

    /**
     * 创建Redis限流服务Bean
     *
     * @param redisTemplate Redis模板
     * @param properties    限流配置属性
     * @param monitor       限流监控器
     * @return Redis限流服务实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitService rateLimitService(StringRedisTemplate redisTemplate,
                                             RateLimitProperties properties,
                                             RateLimitMonitor monitor) {
        log.info("Initializing Rate Limit Service with Redis backend");
        return new RedisRateLimitService(redisTemplate, properties, monitor);
    }

    /**
     * 创建限流切面Bean
     *
     * @param rateLimitService 限流服务
     * @param properties       限流配置属性
     * @return 限流切面实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitAspect rateLimitAspect(RateLimitService rateLimitService,
                                           RateLimitProperties properties) {
        log.info("Initializing Rate Limit Aspect for AOP support");
        return new RateLimitAspect(rateLimitService, properties);
    }
}
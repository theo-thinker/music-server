package com.musicserver.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis配置类
 * 
 * 配置Redis相关组件，包括：
 * 1. RedisTemplate序列化配置
 * 2. 缓存管理器配置
 * 3. Jackson序列化器配置
 * 4. 缓存策略配置
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 设置Key-Value的序列化方式
     * 
     * @param connectionFactory Redis连接工厂
     * @return 配置好的RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 创建Jackson序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // 创建String序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 设置Key的序列化方式 - 使用String序列化
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 设置Value的序列化方式 - 使用Jackson序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 设置默认序列化器
        template.setDefaultSerializer(jackson2JsonRedisSerializer);

        // 启用事务支持
        template.setEnableTransactionSupport(true);

        // 初始化RedisTemplate
        template.afterPropertiesSet();

        log.info("RedisTemplate配置完成");
        return template;
    }

    /**
     * 配置StringRedisTemplate
     * 专门用于String类型的操作，性能更好
     * 
     * @param connectionFactory Redis连接工厂
     * @return StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        log.info("StringRedisTemplate配置完成");
        return template;
    }

    /**
     * 配置缓存管理器
     * 设置缓存的序列化方式和过期策略
     * 
     * @param connectionFactory Redis连接工厂
     * @return Redis缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建Jackson序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // 配置Redis缓存
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            // 设置缓存过期时间
            .entryTtl(Duration.ofMinutes(30))
            // 禁用缓存null值
            .disableCachingNullValues()
            // 设置Key序列化器
            .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            // 设置Value序列化器
            .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                .fromSerializer(jackson2JsonRedisSerializer));

        // 构建缓存管理器
        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                // 设置不同缓存区域的特定配置
                .withCacheConfiguration("userCache", config.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("musicCache", config.entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration("playlistCache", config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("hotMusicCache", config.entryTtl(Duration.ofMinutes(10)))
                .build();

        log.info("Redis缓存管理器配置完成");
        return cacheManager;
    }

    /**
     * 创建Jackson序列化器
     * 配置Jackson的序列化和反序列化规则
     * 
     * @return Jackson2JsonRedisSerializer
     */
    private Jackson2JsonRedisSerializer<Object> createJacksonSerializer() {
        // 创建ObjectMapper并配置
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 设置可见性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 启用类型信息，用于反序列化
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // 注册Java8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        
        // 使用新的构造方法创建序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
            new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        return jackson2JsonRedisSerializer;
    }
}
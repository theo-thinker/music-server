package com.musicserver.utils;

import com.musicserver.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 
 * 封装Redis常用操作，提供便捷的缓存操作方法
 * 支持String、Hash、List、Set、ZSet等数据类型
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    /**
     * Redis模板，用于对象操作
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * String Redis模板，用于字符串操作
     */
    private final StringRedisTemplate stringRedisTemplate;

    // ========================================
    // 通用操作
    // ========================================

    /**
     * 设置过期时间
     * 
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            if (timeout > 0) {
                Boolean result = redisTemplate.expire(key, timeout, unit);
                return Boolean.TRUE.equals(result);
            }
            return false;
        } catch (Exception e) {
            log.error("设置过期时间失败: key={}, timeout={}, unit={}", key, timeout, unit, e);
            return false;
        }
    }

    /**
     * 获取过期时间
     * 
     * @param key 键
     * @return 过期时间（秒），-1表示永不过期，-2表示键不存在
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("获取过期时间失败: key={}", key, e);
            return -2;
        }
    }

    /**
     * 判断键是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("判断键是否存在失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 删除键
     * 
     * @param keys 键列表
     * @return 删除的数量
     */
    public long delete(String... keys) {
        try {
            if (keys != null && keys.length > 0) {
                Long result = redisTemplate.delete(List.of(keys));
                return result != null ? result : 0;
            }
            return 0;
        } catch (Exception e) {
            log.error("删除键失败: keys={}", List.of(keys), e);
            return 0;
        }
    }

    // ========================================
    // String操作
    // ========================================

    /**
     * 获取字符串值
     * 
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        try {
            return key == null ? null : redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取值失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 获取字符串值
     * 
     * @param key 键
     * @return 字符串值
     */
    public String getString(String key) {
        try {
            return key == null ? null : stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取字符串值失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 设置字符串值
     * 
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置值失败: key={}, value={}", key, value, e);
            return false;
        }
    }

    /**
     * 设置字符串值并指定过期时间
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (timeout > 0) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("设置值并指定过期时间失败: key={}, value={}, timeout={}, unit={}", 
                     key, value, timeout, unit, e);
            return false;
        }
    }

    /**
     * 递增
     * 
     * @param key 键
     * @param delta 递增值
     * @return 递增后的值
     */
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("递增失败: key={}, delta={}", key, delta, e);
            return 0;
        }
    }

    /**
     * 递减
     * 
     * @param key 键
     * @param delta 递减值
     * @return 递减后的值
     */
    public long decrement(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("递减失败: key={}, delta={}", key, delta, e);
            return 0;
        }
    }

    // ========================================
    // Hash操作
    // ========================================

    /**
     * 获取Hash值
     * 
     * @param key 键
     * @param field 字段
     * @return 值
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("获取Hash值失败: key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * 获取Hash所有键值对
     * 
     * @param key 键
     * @return 键值对Map
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("获取Hash所有值失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 设置Hash值
     * 
     * @param key 键
     * @param field 字段
     * @param value 值
     * @return 是否成功
     */
    public boolean hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            return true;
        } catch (Exception e) {
            log.error("设置Hash值失败: key={}, field={}, value={}", key, field, value, e);
            return false;
        }
    }

    /**
     * 设置Hash多个值
     * 
     * @param key 键
     * @param map 键值对Map
     * @return 是否成功
     */
    public boolean hSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("设置Hash多个值失败: key={}, map={}", key, map, e);
            return false;
        }
    }

    /**
     * 删除Hash字段
     * 
     * @param key 键
     * @param fields 字段列表
     * @return 删除的数量
     */
    public long hDelete(String key, Object... fields) {
        try {
            Long result = redisTemplate.opsForHash().delete(key, fields);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("删除Hash字段失败: key={}, fields={}", key, List.of(fields), e);
            return 0;
        }
    }

    // ========================================
    // 业务相关方法
    // ========================================

    /**
     * 生成用户缓存键
     * 
     * @param userId 用户ID
     * @return 缓存键
     */
    public String getUserCacheKey(Long userId) {
        return Constants.REDIS_USER_KEY + Constants.REDIS_KEY_SEPARATOR + userId;
    }

    /**
     * 生成音乐缓存键
     * 
     * @param musicId 音乐ID
     * @return 缓存键
     */
    public String getMusicCacheKey(Long musicId) {
        return Constants.REDIS_MUSIC_KEY + Constants.REDIS_KEY_SEPARATOR + musicId;
    }

    /**
     * 生成播放列表缓存键
     * 
     * @param playlistId 播放列表ID
     * @return 缓存键
     */
    public String getPlaylistCacheKey(Long playlistId) {
        return Constants.REDIS_PLAYLIST_KEY + Constants.REDIS_KEY_SEPARATOR + playlistId;
    }

    /**
     * 生成验证码缓存键
     * 
     * @param identifier 标识符（邮箱或手机号）
     * @return 缓存键
     */
    public String getCaptchaCacheKey(String identifier) {
        return Constants.REDIS_CAPTCHA_KEY + Constants.REDIS_KEY_SEPARATOR + identifier;
    }

    /**
     * 生成登录失败缓存键
     * 
     * @param username 用户名
     * @return 缓存键
     */
    public String getLoginFailCacheKey(String username) {
        return Constants.REDIS_LOGIN_FAIL_KEY + Constants.REDIS_KEY_SEPARATOR + username;
    }

    /**
     * 生成用户会话缓存键
     * 
     * @param userId 用户ID
     * @return 缓存键
     */
    public String getUserSessionCacheKey(Long userId) {
        return Constants.REDIS_USER_SESSION_KEY + Constants.REDIS_KEY_SEPARATOR + userId;
    }

    /**
     * 生成JWT黑名单缓存键
     * 
     * @param jti JWT ID
     * @return 缓存键
     */
    public String getJwtBlacklistCacheKey(String jti) {
        return Constants.REDIS_JWT_BLACKLIST_KEY + Constants.REDIS_KEY_SEPARATOR + jti;
    }
}
package com.musicserver.ratelimit.monitor;

import com.musicserver.ratelimit.config.RateLimitProperties;
import com.musicserver.ratelimit.dto.RateLimitStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 限流监控器
 * 
 * 负责收集和统计限流相关的监控数据
 * 提供实时的限流状态监控和历史数据查询
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitMonitor {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    private static final String STATS_KEY_PREFIX = "rate_limit:stats:";
    private static final String HOURLY_STATS_KEY = STATS_KEY_PREFIX + "hourly:";
    private static final String DAILY_STATS_KEY = STATS_KEY_PREFIX + "daily:";
    private static final String HOTSPOT_STATS_KEY = STATS_KEY_PREFIX + "hotspot:";
    private static final String ALERT_KEY_PREFIX = "rate_limit:alerts:";

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 记录限流事件
     * 
     * @param key 限流key
     * @param allowed 是否允许通过
     * @param remaining 剩余配额
     * @param limit 限流阈值
     * @param strategy 限流策略
     * @param isHotspot 是否为热点
     * @param clientIp 客户端IP
     */
    public void recordRateLimitEvent(String key, boolean allowed, long remaining, long limit, 
                                   String strategy, boolean isHotspot, String clientIp) {
        if (!properties.getEnableMonitor()) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            String hourKey = HOURLY_STATS_KEY + now.format(HOUR_FORMATTER);
            String dayKey = DAILY_STATS_KEY + now.format(DAY_FORMATTER);

            // 记录小时级统计
            recordHourlyStats(hourKey, key, allowed, isHotspot, strategy);
            
            // 记录日级统计
            recordDailyStats(dayKey, key, allowed, isHotspot, strategy);
            
            // 记录热点数据统计
            if (isHotspot) {
                recordHotspotStats(key, clientIp, now);
            }
            
            // 检查是否需要触发告警
            checkAndTriggerAlerts(key, allowed, remaining, limit, isHotspot);
            
        } catch (Exception e) {
            log.error("记录限流事件失败: key={}", key, e);
        }
    }

    /**
     * 记录小时级统计
     */
    private void recordHourlyStats(String hourKey, String key, boolean allowed, boolean isHotspot, String strategy) {
        // 总请求数
        redisTemplate.opsForHash().increment(hourKey, "total_requests", 1);
        
        // 按状态统计
        if (allowed) {
            redisTemplate.opsForHash().increment(hourKey, "allowed_requests", 1);
        } else {
            redisTemplate.opsForHash().increment(hourKey, "blocked_requests", 1);
        }
        
        // 按策略统计
        redisTemplate.opsForHash().increment(hourKey, "strategy:" + strategy, 1);
        
        // 热点请求统计
        if (isHotspot) {
            redisTemplate.opsForHash().increment(hourKey, "hotspot_requests", 1);
        }
        
        // 按key统计
        redisTemplate.opsForHash().increment(hourKey, "key:" + key, 1);
        
        // 设置过期时间：保留7天
        redisTemplate.expire(hourKey, 7, TimeUnit.DAYS);
    }

    /**
     * 记录日级统计
     */
    private void recordDailyStats(String dayKey, String key, boolean allowed, boolean isHotspot, String strategy) {
        // 总请求数
        redisTemplate.opsForHash().increment(dayKey, "total_requests", 1);
        
        // 按状态统计
        if (allowed) {
            redisTemplate.opsForHash().increment(dayKey, "allowed_requests", 1);
        } else {
            redisTemplate.opsForHash().increment(dayKey, "blocked_requests", 1);
        }
        
        // 按策略统计
        redisTemplate.opsForHash().increment(dayKey, "strategy:" + strategy, 1);
        
        // 热点请求统计
        if (isHotspot) {
            redisTemplate.opsForHash().increment(dayKey, "hotspot_requests", 1);
        }
        
        // 设置过期时间：保留30天
        redisTemplate.expire(dayKey, 30, TimeUnit.DAYS);
    }

    /**
     * 记录热点数据统计
     */
    private void recordHotspotStats(String key, String clientIp, LocalDateTime time) {
        String hotspotKey = HOTSPOT_STATS_KEY + time.format(DAY_FORMATTER);
        
        // 记录热点key的访问次数
        redisTemplate.opsForZSet().incrementScore(hotspotKey + ":keys", key, 1);
        
        // 记录热点IP的访问次数
        if (clientIp != null) {
            redisTemplate.opsForZSet().incrementScore(hotspotKey + ":ips", clientIp, 1);
        }
        
        // 设置过期时间：保留7天
        redisTemplate.expire(hotspotKey + ":keys", 7, TimeUnit.DAYS);
        redisTemplate.expire(hotspotKey + ":ips", 7, TimeUnit.DAYS);
    }

    /**
     * 检查并触发告警
     */
    private void checkAndTriggerAlerts(String key, boolean allowed, long remaining, long limit, boolean isHotspot) {
        if (!allowed) {
            // 限流触发告警
            String alertKey = ALERT_KEY_PREFIX + "blocked:" + LocalDateTime.now().format(HOUR_FORMATTER);
            redisTemplate.opsForHash().increment(alertKey, key, 1);
            redisTemplate.expire(alertKey, 24, TimeUnit.HOURS);
            
            // 检查是否达到告警阈值
            String countStr = (String) redisTemplate.opsForHash().get(alertKey, key);
            long count = countStr != null ? Long.parseLong(countStr) : 0;
            
            // 如果1小时内同一个key被限流超过100次，记录高频告警
            if (count > 100) {
                recordHighFrequencyAlert(key, count);
            }
        }
        
        // 热点数据告警
        if (isHotspot) {
            String hotspotAlertKey = ALERT_KEY_PREFIX + "hotspot:" + LocalDateTime.now().format(HOUR_FORMATTER);
            redisTemplate.opsForHash().increment(hotspotAlertKey, key, 1);
            redisTemplate.expire(hotspotAlertKey, 24, TimeUnit.HOURS);
        }
        
        // 配额低告警（剩余配额不足20%）
        if (allowed && remaining < limit * 0.2) {
            String lowQuotaAlertKey = ALERT_KEY_PREFIX + "low_quota:" + LocalDateTime.now().format(HOUR_FORMATTER);
            redisTemplate.opsForSet().add(lowQuotaAlertKey, key);
            redisTemplate.expire(lowQuotaAlertKey, 24, TimeUnit.HOURS);
        }
    }

    /**
     * 记录高频告警
     */
    private void recordHighFrequencyAlert(String key, long count) {
        String highFreqAlertKey = ALERT_KEY_PREFIX + "high_frequency";
        Map<String, String> alertData = new HashMap<>();
        alertData.put("key", key);
        alertData.put("count", String.valueOf(count));
        alertData.put("time", LocalDateTime.now().toString());
        
        redisTemplate.opsForList().leftPush(highFreqAlertKey, 
            alertData.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(",")));
        
        // 只保留最近1000条告警记录
        redisTemplate.opsForList().trim(highFreqAlertKey, 0, 999);
        redisTemplate.expire(highFreqAlertKey, 7, TimeUnit.DAYS);
    }

    /**
     * 获取限流统计信息
     * 
     * @param type 统计类型 (hourly/daily)
     * @param time 时间字符串
     * @return 统计信息
     */
    public RateLimitStatistics getStatistics(String type, String time) {
        try {
            String statsKey;
            if ("hourly".equals(type)) {
                statsKey = HOURLY_STATS_KEY + time;
            } else if ("daily".equals(type)) {
                statsKey = DAILY_STATS_KEY + time;
            } else {
                throw new IllegalArgumentException("Unsupported statistics type: " + type);
            }

            Map<Object, Object> rawStats = redisTemplate.opsForHash().entries(statsKey);
            return buildStatistics(rawStats, type, time);
            
        } catch (Exception e) {
            log.error("获取限流统计信息失败: type={}, time={}", type, time, e);
            return RateLimitStatistics.empty(type, time);
        }
    }

    /**
     * 获取热点数据统计
     * 
     * @param date 日期字符串 (yyyy-MM-dd)
     * @param topN 返回前N个热点
     * @return 热点统计
     */
    public Map<String, Object> getHotspotStatistics(String date, int topN) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            String hotspotKeysKey = HOTSPOT_STATS_KEY + date + ":keys";
            String hotspotIpsKey = HOTSPOT_STATS_KEY + date + ":ips";
            
            // 获取热点keys
            Set<String> topKeys = redisTemplate.opsForZSet().reverseRange(hotspotKeysKey, 0, topN - 1);
            List<Map<String, Object>> topKeysList = new ArrayList<>();
            if (topKeys != null) {
                for (String key : topKeys) {
                    Double score = redisTemplate.opsForZSet().score(hotspotKeysKey, key);
                    Map<String, Object> keyInfo = new HashMap<>();
                    keyInfo.put("key", key);
                    keyInfo.put("count", score != null ? score.longValue() : 0);
                    topKeysList.add(keyInfo);
                }
            }
            result.put("topKeys", topKeysList);
            
            // 获取热点IPs
            Set<String> topIps = redisTemplate.opsForZSet().reverseRange(hotspotIpsKey, 0, topN - 1);
            List<Map<String, Object>> topIpsList = new ArrayList<>();
            if (topIps != null) {
                for (String ip : topIps) {
                    Double score = redisTemplate.opsForZSet().score(hotspotIpsKey, ip);
                    Map<String, Object> ipInfo = new HashMap<>();
                    ipInfo.put("ip", ip);
                    ipInfo.put("count", score != null ? score.longValue() : 0);
                    topIpsList.add(ipInfo);
                }
            }
            result.put("topIps", topIpsList);
            
            result.put("date", date);
            result.put("topN", topN);
            
            return result;
            
        } catch (Exception e) {
            log.error("获取热点数据统计失败: date={}", date, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 获取告警信息
     * 
     * @param alertType 告警类型
     * @param time 时间字符串
     * @return 告警信息
     */
    public Map<String, Object> getAlerts(String alertType, String time) {
        try {
            Map<String, Object> result = new HashMap<>();
            String alertKey = ALERT_KEY_PREFIX + alertType + ":" + time;
            
            switch (alertType) {
                case "blocked":
                case "hotspot":
                    Map<Object, Object> alerts = redisTemplate.opsForHash().entries(alertKey);
                    result.put("alerts", alerts);
                    break;
                    
                case "low_quota":
                    Set<String> lowQuotaKeys = redisTemplate.opsForSet().members(alertKey);
                    result.put("lowQuotaKeys", lowQuotaKeys);
                    break;
                    
                case "high_frequency":
                    List<String> highFreqAlerts = redisTemplate.opsForList().range(alertKey, 0, -1);
                    result.put("highFrequencyAlerts", highFreqAlerts);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unsupported alert type: " + alertType);
            }
            
            result.put("alertType", alertType);
            result.put("time", time);
            
            return result;
            
        } catch (Exception e) {
            log.error("获取告警信息失败: alertType={}, time={}", alertType, time, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 构建统计信息对象
     */
    private RateLimitStatistics buildStatistics(Map<Object, Object> rawStats, String type, String time) {
        RateLimitStatistics stats = new RateLimitStatistics();
        stats.setType(type);
        stats.setTime(time);
        
        // 基础统计
        stats.setTotalRequests(getLongValue(rawStats, "total_requests"));
        stats.setAllowedRequests(getLongValue(rawStats, "allowed_requests"));
        stats.setBlockedRequests(getLongValue(rawStats, "blocked_requests"));
        stats.setHotspotRequests(getLongValue(rawStats, "hotspot_requests"));
        
        // 计算比率
        if (stats.getTotalRequests() > 0) {
            stats.setBlockRate(stats.getBlockedRequests() * 100.0 / stats.getTotalRequests());
            stats.setHotspotRate(stats.getHotspotRequests() * 100.0 / stats.getTotalRequests());
        }
        
        // 按策略统计
        Map<String, Long> strategyStats = new HashMap<>();
        Map<String, Long> keyStats = new HashMap<>();
        
        for (Map.Entry<Object, Object> entry : rawStats.entrySet()) {
            String key = entry.getKey().toString();
            Long value = getLongValue(entry.getValue());
            
            if (key.startsWith("strategy:")) {
                strategyStats.put(key.substring(9), value);
            } else if (key.startsWith("key:")) {
                keyStats.put(key.substring(4), value);
            }
        }
        
        stats.setStrategyStats(strategyStats);
        stats.setKeyStats(keyStats);
        
        return stats;
    }

    /**
     * 安全地获取Long值
     */
    private Long getLongValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        return getLongValue(value);
    }

    private Long getLongValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 清理过期的监控数据
     */
    public void cleanupExpiredData() {
        try {
            // 清理过期的小时级数据（保留7天）
            LocalDateTime cutoffHour = LocalDateTime.now().minusDays(7);
            String cutoffHourStr = cutoffHour.format(HOUR_FORMATTER);
            
            // 清理过期的日级数据（保留30天）
            LocalDateTime cutoffDay = LocalDateTime.now().minusDays(30);
            String cutoffDayStr = cutoffDay.format(DAY_FORMATTER);
            
            // 实际清理逻辑由Redis的TTL自动处理，这里只是记录清理动作
            log.info("清理过期监控数据: hourly cutoff={}, daily cutoff={}", cutoffHourStr, cutoffDayStr);
            
        } catch (Exception e) {
            log.error("清理过期监控数据失败", e);
        }
    }
}
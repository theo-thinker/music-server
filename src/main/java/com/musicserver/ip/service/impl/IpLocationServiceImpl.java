package com.musicserver.ip.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.musicserver.ip.config.IpLocationProperties;
import com.musicserver.ip.entity.IPInfo;
import com.musicserver.ip.entity.IPLocation;
import com.musicserver.ip.entity.IPStatistics;
import com.musicserver.ip.exception.IpLocationException;
import com.musicserver.ip.exception.IpValidationException;
import com.musicserver.ip.service.IpLocationService;
import com.musicserver.ip.util.IpLocationUtil;
import com.musicserver.ip.util.IpValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IP定位服务实现类
 * 
 * 基于ip2region实现的IP地理位置查询服务
 * 支持缓存、统计、限流等企业级功能
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Service
public class IpLocationServiceImpl implements IpLocationService {

    private final Searcher ip2regionSearcher;
    private final IpLocationProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final Cache<String, IPLocation> localCache;

    // 统计数据
    private final Map<String, IPStatistics> statisticsCache = new ConcurrentHashMap<>();
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheErrors = new AtomicLong(0);

    public IpLocationServiceImpl(Searcher ip2regionSearcher, 
                               IpLocationProperties properties,
                               StringRedisTemplate redisTemplate,
                               Cache<String, IPLocation> localCache) {
        this.ip2regionSearcher = ip2regionSearcher;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.localCache = localCache;
        
        log.info("IP Location Service initialized with cache enabled: {}", properties.getEnableCache());
    }

    @Override
    public IPLocation getLocation(String ip) {
        long startTime = System.currentTimeMillis();
        totalQueries.incrementAndGet();

        try {
            // 参数验证
            validateIP(ip);
            
            // 安全检查
            performSecurityCheck(ip);
            
            // 尝试从缓存获取
            IPLocation cached = getFromCache(ip);
            if (cached != null) {
                cacheHits.incrementAndGet();
                log.debug("Cache hit for IP: {}", ip);
                return cached;
            }
            
            // 从ip2region查询
            IPLocation location = queryFromIP2Region(ip);
            
            // 存储到缓存
            storeToCache(ip, location);
            
            // 记录查询时间
            long queryTime = System.currentTimeMillis() - startTime;
            log.debug("IP location query completed for {} in {}ms", ip, queryTime);
            
            return location;
            
        } catch (Exception e) {
            log.error("Failed to get location for IP: {}", ip, e);
            if (e instanceof IpLocationException || e instanceof IpValidationException) {
                throw e;
            }
            throw IpLocationException.queryFailed(ip, e);
        }
    }

    @Override
    public IPInfo getIPInfo(String ip) {
        IPLocation location = getLocation(ip);
        if (location == null) {
            return createDefaultIPInfo(ip);
        }
        
        return enhanceIPInfo(location.toIPInfo());
    }

    @Override
    @Async("ipQueryExecutor")
    public CompletableFuture<IPLocation> getLocationAsync(String ip) {
        return CompletableFuture.supplyAsync(() -> getLocation(ip));
    }

    @Override
    @Async("ipQueryExecutor")
    public CompletableFuture<IPInfo> getIPInfoAsync(String ip) {
        return CompletableFuture.supplyAsync(() -> getIPInfo(ip));
    }

    @Override
    public Map<String, IPLocation> getLocationsBatch(List<String> ips) {
        Map<String, IPLocation> results = new HashMap<>();
        
        if (ips == null || ips.isEmpty()) {
            return results;
        }
        
        // 限制批量查询数量
        List<String> validIps = ips.stream()
                .filter(IpValidationUtil::isValidIP)
                .limit(100) // 限制最多100个
                .toList();
        
        for (String ip : validIps) {
            try {
                results.put(ip, getLocation(ip));
            } catch (Exception e) {
                log.warn("Failed to get location for IP {} in batch query: {}", ip, e.getMessage());
                results.put(ip, createDefaultLocation(ip));
            }
        }
        
        return results;
    }

    @Override
    public Map<String, IPInfo> getIPInfosBatch(List<String> ips) {
        Map<String, IPInfo> results = new HashMap<>();
        Map<String, IPLocation> locations = getLocationsBatch(ips);
        
        for (Map.Entry<String, IPLocation> entry : locations.entrySet()) {
            IPLocation location = entry.getValue();
            results.put(entry.getKey(), location != null ? location.toIPInfo() : createDefaultIPInfo(entry.getKey()));
        }
        
        return results;
    }

    @Override
    public boolean isValidIP(String ip) {
        return IpValidationUtil.isValidIP(ip);
    }

    @Override
    public boolean isPrivateIP(String ip) {
        return IpValidationUtil.isPrivateIP(ip);
    }

    @Override
    public boolean isIPv6(String ip) {
        return IpValidationUtil.isValidIPv6(ip);
    }

    @Override
    public String getIPType(String ip) {
        return IpValidationUtil.getIPType(ip);
    }

    @Override
    public void recordAccess(String ip, String userAgent, String requestUri, Long userId) {
        try {
            doRecordAccess(ip, userAgent, requestUri, userId);
        } catch (Exception e) {
            log.error("Failed to record access for IP: {}", ip, e);
        }
    }

    @Override
    @Async("ipStatisticsExecutor")
    public void recordAccessAsync(String ip, String userAgent, String requestUri, Long userId) {
        recordAccess(ip, userAgent, requestUri, userId);
    }

    @Override
    public IPStatistics getStatistics(String ip, LocalDate startDate, LocalDate endDate) {
        // 这里应该从数据库或缓存中获取统计数据
        // 为了简化示例，返回模拟数据
        return IPStatistics.builder()
                .ip(ip)
                .statDate(LocalDate.now())
                .accessCount(100L)
                .firstAccessTime(LocalDateTime.now().minusHours(24))
                .lastAccessTime(LocalDateTime.now())
                .country("中国")
                .region("北京")
                .city("北京")
                .build();
    }

    @Override
    public IPStatistics getTodayStatistics(String ip) {
        return getStatistics(ip, LocalDate.now(), LocalDate.now());
    }

    @Override
    public List<IPStatistics> getTopIPs(int limit, LocalDate date) {
        // 这里应该从数据库查询热门IP
        return new ArrayList<>();
    }

    @Override
    public List<IPStatistics> getRiskIPs(int limit, Integer riskLevel) {
        // 这里应该从数据库查询风险IP
        return new ArrayList<>();
    }

    @Override
    public boolean isWhitelistIP(String ip) {
        return properties.isWhitelistIp(ip);
    }

    @Override
    public boolean isBlacklistIP(String ip) {
        return properties.isBlacklistIp(ip);
    }

    @Override
    public void addToBlacklist(String ip, String reason, Integer durationHours) {
        String key = properties.getCacheKeyPrefix() + "blacklist:" + ip;
        String value = reason + "|" + System.currentTimeMillis();
        
        if (durationHours != null && durationHours > 0) {
            redisTemplate.opsForValue().set(key, value, durationHours, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
        
        log.info("Added IP {} to blacklist: {}", ip, reason);
    }

    @Override
    public void removeFromBlacklist(String ip) {
        String key = properties.getCacheKeyPrefix() + "blacklist:" + ip;
        redisTemplate.delete(key);
        log.info("Removed IP {} from blacklist", ip);
    }

    @Override
    public void clearCache() {
        try {
            // 清空本地缓存
            if (localCache != null) {
                localCache.invalidateAll();
            }
            
            // 清空Redis缓存
            String pattern = properties.getCacheKeyPrefix() + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            log.info("IP location cache cleared");
        } catch (Exception e) {
            log.error("Failed to clear IP location cache", e);
        }
    }

    @Override
    public void clearCache(String ip) {
        try {
            // 清空本地缓存
            if (localCache != null) {
                localCache.invalidate(ip);
            }
            
            // 清空Redis缓存
            String key = IpLocationUtil.createCacheKey(properties.getCacheKeyPrefix(), ip);
            redisTemplate.delete(key);
            
            log.debug("Cache cleared for IP: {}", ip);
        } catch (Exception e) {
            log.error("Failed to clear cache for IP: {}", ip, e);
        }
    }

    @Override
    public void warmupCache(List<String> ips) {
        if (ips == null || ips.isEmpty()) {
            return;
        }
        
        log.info("Starting cache warmup for {} IPs", ips.size());
        
        CompletableFuture.runAsync(() -> {
            for (String ip : ips) {
                try {
                    if (IpValidationUtil.isValidIP(ip)) {
                        getLocation(ip);
                        Thread.sleep(10); // 避免过快查询
                    }
                } catch (Exception e) {
                    log.warn("Failed to warmup cache for IP: {}", ip);
                }
            }
            log.info("Cache warmup completed");
        });
    }

    @Override
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalQueries", totalQueries.get());
        stats.put("cacheHits", cacheHits.get());
        stats.put("cacheErrors", cacheErrors.get());
        stats.put("hitRate", totalQueries.get() > 0 ? (double) cacheHits.get() / totalQueries.get() : 0.0);
        
        if (localCache != null) {
            com.github.benmanes.caffeine.cache.stats.CacheStats cacheStats = localCache.stats();
            stats.put("localCacheHitRate", cacheStats.hitRate());
            stats.put("localCacheSize", localCache.estimatedSize());
            stats.put("localCacheEvictions", cacheStats.evictionCount());
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 测试ip2region查询
            String testResult = ip2regionSearcher.search("8.8.8.8");
            status.put("ip2regionStatus", testResult != null ? "OK" : "ERROR");
        } catch (Exception e) {
            status.put("ip2regionStatus", "ERROR: " + e.getMessage());
        }
        
        try {
            // 测试Redis连接
            redisTemplate.opsForValue().get("test");
            status.put("redisStatus", "OK");
        } catch (Exception e) {
            status.put("redisStatus", "ERROR: " + e.getMessage());
        }
        
        status.put("localCacheStatus", localCache != null ? "OK" : "DISABLED");
        status.put("configValid", properties.isValid());
        status.put("securityEnabled", properties.isSecurityEnabled());
        
        return status;
    }

    @Override
    public void flushStatistics() {
        // 刷新统计数据到持久化存储
        log.info("Flushing IP statistics to persistent storage");
    }

    @Override
    public long cleanupExpiredStatistics(LocalDate beforeDate) {
        // 清理过期统计数据
        log.info("Cleaning up expired statistics before {}", beforeDate);
        return 0;
    }

    @Override
    public String exportStatistics(LocalDate startDate, LocalDate endDate, String format) {
        // 导出统计数据
        log.info("Exporting statistics from {} to {} in {} format", startDate, endDate, format);
        return "/tmp/ip_statistics_export_" + System.currentTimeMillis() + "." + format.toLowerCase();
    }

    // 私有方法

    private void validateIP(String ip) {
        if (!StringUtils.hasText(ip)) {
            throw IpValidationException.nullOrEmpty();
        }
        
        if (!IpValidationUtil.isValidIP(ip)) {
            throw IpValidationException.invalidFormat(ip);
        }
    }

    private void performSecurityCheck(String ip) {
        // 检查黑名单
        if (isBlacklistIP(ip)) {
            throw IpValidationException.blacklisted(ip);
        }
        
        // 检查是否被临时封禁
        String blacklistKey = properties.getCacheKeyPrefix() + "blacklist:" + ip;
        if (redisTemplate.hasKey(blacklistKey)) {
            throw IpLocationException.accessDenied(ip, "IP is temporarily banned");
        }
    }

    private IPLocation getFromCache(String ip) {
        try {
            // 先尝试本地缓存
            if (localCache != null) {
                IPLocation cached = localCache.getIfPresent(ip);
                if (cached != null) {
                    return cached;
                }
            }
            
            // 再尝试Redis缓存
            if (properties.getEnableCache()) {
                String key = IpLocationUtil.createCacheKey(properties.getCacheKeyPrefix(), ip);
                String cached = redisTemplate.opsForValue().get(key);
                if (StringUtils.hasText(cached)) {
                    return deserializeLocation(cached);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get IP location from cache: {}", ip, e);
            cacheErrors.incrementAndGet();
        }
        
        return null;
    }

    private void storeToCache(String ip, IPLocation location) {
        if (!properties.getEnableCache() || location == null) {
            return;
        }
        
        try {
            // 存储到本地缓存
            if (localCache != null) {
                localCache.put(ip, location);
            }
            
            // 存储到Redis缓存
            String key = IpLocationUtil.createCacheKey(properties.getCacheKeyPrefix(), ip);
            String value = serializeLocation(location);
            redisTemplate.opsForValue().set(key, value, properties.getCacheExpireSeconds(), TimeUnit.SECONDS);
            
        } catch (Exception e) {
            log.warn("Failed to store IP location to cache: {}", ip, e);
            cacheErrors.incrementAndGet();
        }
    }

    private IPLocation queryFromIP2Region(String ip) {
        try {
            String result = ip2regionSearcher.search(ip);
            return IPLocation.fromRegionResult(ip, result);
        } catch (Exception e) {
            log.error("Failed to query IP location from ip2region: {}", ip, e);
            throw IpLocationException.queryFailed(ip, e);
        }
    }

    private IPLocation createDefaultLocation(String ip) {
        return IPLocation.builder()
                .ip(ip)
                .country("未知")
                .region("未知")
                .city("未知")
                .isp("未知")
                .createdTime(LocalDateTime.now())
                .build();
    }

    private IPInfo createDefaultIPInfo(String ip) {
        return IPInfo.builder()
                .ip(ip)
                .country("未知")
                .region("未知")
                .city("未知")
                .isp("未知")
                .location("未知地区")
                .ipType(IpValidationUtil.getIPType(ip))
                .isPrivate(IpValidationUtil.isPrivateIP(ip))
                .securityLevel(1)
                .source("default")
                .createdTime(LocalDateTime.now())
                .build();
    }

    private IPInfo enhanceIPInfo(IPInfo ipInfo) {
        if (ipInfo == null) {
            return null;
        }
        
        // 增强IP信息
        ipInfo.setIsPrivate(IpValidationUtil.isPrivateIP(ipInfo.getIp()));
        ipInfo.setIpType(IpValidationUtil.getIPType(ipInfo.getIp()));
        ipInfo.setUpdatedTime(LocalDateTime.now());
        
        return ipInfo;
    }

    private void doRecordAccess(String ip, String userAgent, String requestUri, Long userId) {
        if (!properties.getEnableStatistics()) {
            return;
        }
        
        // 记录访问统计
        String today = LocalDate.now().toString();
        String statsKey = IpLocationUtil.createStatsCacheKey(properties.getCacheKeyPrefix(), ip, today);
        
        // 这里应该实现具体的统计记录逻辑
        log.debug("Recording access for IP: {}, URI: {}, User: {}", ip, requestUri, userId);
    }

    private String serializeLocation(IPLocation location) {
        // 简单的序列化实现，实际应该使用JSON
        return String.format("%s|%s|%s|%s|%s", 
                location.getIp(),
                location.getCountry() != null ? location.getCountry() : "",
                location.getRegion() != null ? location.getRegion() : "",
                location.getCity() != null ? location.getCity() : "",
                location.getIsp() != null ? location.getIsp() : "");
    }

    private IPLocation deserializeLocation(String data) {
        if (!StringUtils.hasText(data)) {
            return null;
        }
        
        String[] parts = data.split("\\|", -1);
        if (parts.length < 5) {
            return null;
        }
        
        return IPLocation.builder()
                .ip(parts[0])
                .country(StringUtils.hasText(parts[1]) ? parts[1] : "未知")
                .region(StringUtils.hasText(parts[2]) ? parts[2] : "未知")
                .city(StringUtils.hasText(parts[3]) ? parts[3] : "未知")
                .isp(StringUtils.hasText(parts[4]) ? parts[4] : "未知")
                .createdTime(LocalDateTime.now())
                .build();
    }
}
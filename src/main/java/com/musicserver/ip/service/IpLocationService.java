package com.musicserver.ip.service;

import com.musicserver.ip.entity.IPInfo;
import com.musicserver.ip.entity.IPLocation;
import com.musicserver.ip.entity.IPStatistics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * IP定位服务接口
 * 
 * 提供IP地理位置查询、缓存、统计等功能
 * 支持同步和异步查询模式
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public interface IpLocationService {

    /**
     * 根据IP地址查询地理位置信息
     * 
     * @param ip IP地址
     * @return IP位置信息
     */
    IPLocation getLocation(String ip);

    /**
     * 根据IP地址查询详细信息
     * 
     * @param ip IP地址
     * @return IP详细信息
     */
    IPInfo getIPInfo(String ip);

    /**
     * 异步查询IP地理位置信息
     * 
     * @param ip IP地址
     * @return IP位置信息的Future
     */
    CompletableFuture<IPLocation> getLocationAsync(String ip);

    /**
     * 异步查询IP详细信息
     * 
     * @param ip IP地址
     * @return IP详细信息的Future
     */
    CompletableFuture<IPInfo> getIPInfoAsync(String ip);

    /**
     * 批量查询IP地理位置信息
     * 
     * @param ips IP地址列表
     * @return IP地址到位置信息的映射
     */
    Map<String, IPLocation> getLocationsBatch(List<String> ips);

    /**
     * 批量查询IP详细信息
     * 
     * @param ips IP地址列表
     * @return IP地址到详细信息的映射
     */
    Map<String, IPInfo> getIPInfosBatch(List<String> ips);

    /**
     * 验证IP地址格式
     * 
     * @param ip IP地址
     * @return 是否为有效的IP地址
     */
    boolean isValidIP(String ip);

    /**
     * 判断是否为内网IP
     * 
     * @param ip IP地址
     * @return 是否为内网IP
     */
    boolean isPrivateIP(String ip);

    /**
     * 判断是否为IPv6地址
     * 
     * @param ip IP地址
     * @return 是否为IPv6地址
     */
    boolean isIPv6(String ip);

    /**
     * 获取IP地址类型
     * 
     * @param ip IP地址
     * @return IP地址类型：IPv4/IPv6/INVALID
     */
    String getIPType(String ip);

    /**
     * 记录IP访问
     * 
     * @param ip IP地址
     * @param userAgent 用户代理
     * @param requestUri 请求URI
     * @param userId 用户ID（可选）
     */
    void recordAccess(String ip, String userAgent, String requestUri, Long userId);

    /**
     * 异步记录IP访问
     * 
     * @param ip IP地址
     * @param userAgent 用户代理
     * @param requestUri 请求URI
     * @param userId 用户ID（可选）
     */
    void recordAccessAsync(String ip, String userAgent, String requestUri, Long userId);

    /**
     * 获取IP访问统计
     * 
     * @param ip IP地址
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return IP统计信息
     */
    IPStatistics getStatistics(String ip, LocalDate startDate, LocalDate endDate);

    /**
     * 获取IP今日统计
     * 
     * @param ip IP地址
     * @return 今日统计信息
     */
    IPStatistics getTodayStatistics(String ip);

    /**
     * 获取热门IP列表
     * 
     * @param limit 返回数量限制
     * @param date 统计日期
     * @return 热门IP列表
     */
    List<IPStatistics> getTopIPs(int limit, LocalDate date);

    /**
     * 获取风险IP列表
     * 
     * @param limit 返回数量限制
     * @param riskLevel 风险等级（1-安全，2-可疑，3-危险）
     * @return 风险IP列表
     */
    List<IPStatistics> getRiskIPs(int limit, Integer riskLevel);

    /**
     * 检查IP是否在白名单中
     * 
     * @param ip IP地址
     * @return 是否在白名单中
     */
    boolean isWhitelistIP(String ip);

    /**
     * 检查IP是否在黑名单中
     * 
     * @param ip IP地址
     * @return 是否在黑名单中
     */
    boolean isBlacklistIP(String ip);

    /**
     * 添加IP到黑名单
     * 
     * @param ip IP地址
     * @param reason 封禁原因
     * @param durationHours 封禁时长（小时）
     */
    void addToBlacklist(String ip, String reason, Integer durationHours);

    /**
     * 从黑名单移除IP
     * 
     * @param ip IP地址
     */
    void removeFromBlacklist(String ip);

    /**
     * 清空缓存
     */
    void clearCache();

    /**
     * 清空指定IP的缓存
     * 
     * @param ip IP地址
     */
    void clearCache(String ip);

    /**
     * 预热缓存
     * 
     * @param ips IP地址列表
     */
    void warmupCache(List<String> ips);

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    Map<String, Object> getCacheStats();

    /**
     * 获取服务健康状态
     * 
     * @return 健康状态信息
     */
    Map<String, Object> getHealthStatus();

    /**
     * 刷新统计数据
     */
    void flushStatistics();

    /**
     * 清理过期统计数据
     * 
     * @param beforeDate 清理此日期之前的数据
     * @return 清理的记录数
     */
    long cleanupExpiredStatistics(LocalDate beforeDate);

    /**
     * 导出统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param format 导出格式（CSV/JSON/EXCEL）
     * @return 导出文件路径
     */
    String exportStatistics(LocalDate startDate, LocalDate endDate, String format);
}
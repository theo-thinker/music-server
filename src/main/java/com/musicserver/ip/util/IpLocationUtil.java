package com.musicserver.ip.util;

import com.musicserver.ip.entity.IPInfo;
import com.musicserver.ip.entity.IPLocation;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * IP定位工具类
 * 
 * 提供IP定位相关的工具方法
 * 包括IP获取、格式化、统计等功能
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class IpLocationUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private IpLocationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 从HttpServletRequest中获取真实的IP地址
     * 
     * @param request HTTP请求对象
     * @return 真实的IP地址
     */
    public static String getRealIP(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        // 尝试从各种可能的头部获取真实IP
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_REAL_IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (isValidRealIP(ip)) {
                // X-Forwarded-For可能包含多个IP，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                if (isValidRealIP(ip)) {
                    return ip;
                }
            }
        }

        // 如果所有头部都没有，使用request.getRemoteAddr()
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : "";
    }

    /**
     * 验证是否为有效的真实IP地址
     * 
     * @param ip IP地址
     * @return 是否有效
     */
    private static boolean isValidRealIP(String ip) {
        return StringUtils.hasText(ip) && 
               !"unknown".equalsIgnoreCase(ip) && 
               !"null".equalsIgnoreCase(ip) &&
               IpValidationUtil.isValidIP(ip);
    }

    /**
     * 获取用户代理信息
     * 
     * @param request HTTP请求对象
     * @return 用户代理字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : "";
    }

    /**
     * 解析用户代理信息
     * 
     * @param userAgent 用户代理字符串
     * @return 解析后的设备信息
     */
    public static Map<String, String> parseUserAgent(String userAgent) {
        Map<String, String> result = new HashMap<>();
        
        if (!StringUtils.hasText(userAgent)) {
            result.put("browser", "Unknown");
            result.put("os", "Unknown");
            result.put("device", "Unknown");
            return result;
        }

        String lowerUserAgent = userAgent.toLowerCase();
        
        // 解析浏览器
        String browser = parseBrowser(lowerUserAgent);
        result.put("browser", browser);
        
        // 解析操作系统
        String os = parseOperatingSystem(lowerUserAgent);
        result.put("os", os);
        
        // 解析设备类型
        String device = parseDeviceType(lowerUserAgent);
        result.put("device", device);
        
        return result;
    }

    /**
     * 解析浏览器类型
     * 
     * @param userAgent 用户代理字符串（小写）
     * @return 浏览器类型
     */
    private static String parseBrowser(String userAgent) {
        if (userAgent.contains("chrome") && !userAgent.contains("edg")) {
            return "Chrome";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari";
        } else if (userAgent.contains("edg")) {
            return "Edge";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }

    /**
     * 解析操作系统
     * 
     * @param userAgent 用户代理字符串（小写）
     * @return 操作系统类型
     */
    private static String parseOperatingSystem(String userAgent) {
        if (userAgent.contains("windows")) {
            if (userAgent.contains("windows nt 10")) {
                return "Windows 10/11";
            } else if (userAgent.contains("windows nt 6.3")) {
                return "Windows 8.1";
            } else if (userAgent.contains("windows nt 6.2")) {
                return "Windows 8";
            } else if (userAgent.contains("windows nt 6.1")) {
                return "Windows 7";
            } else {
                return "Windows";
            }
        } else if (userAgent.contains("mac os x")) {
            return "macOS";
        } else if (userAgent.contains("linux")) {
            if (userAgent.contains("android")) {
                return "Android";
            } else {
                return "Linux";
            }
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else {
            return "Unknown";
        }
    }

    /**
     * 解析设备类型
     * 
     * @param userAgent 用户代理字符串（小写）
     * @return 设备类型
     */
    private static String parseDeviceType(String userAgent) {
        if (userAgent.contains("mobile") || userAgent.contains("android") || 
            userAgent.contains("iphone")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    /**
     * 格式化IP信息为字符串
     * 
     * @param ipInfo IP信息
     * @return 格式化后的字符串
     */
    public static String formatIPInfo(IPInfo ipInfo) {
        if (ipInfo == null) {
            return "N/A";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("IP: ").append(ipInfo.getIp());
        
        if (StringUtils.hasText(ipInfo.getLocation())) {
            sb.append(" | 位置: ").append(ipInfo.getLocation());
        }
        
        if (StringUtils.hasText(ipInfo.getIsp())) {
            sb.append(" | ISP: ").append(ipInfo.getIsp());
        }
        
        if (ipInfo.getIsPrivate() != null && ipInfo.getIsPrivate()) {
            sb.append(" | 内网");
        }
        
        return sb.toString();
    }

    /**
     * 创建缓存键
     * 
     * @param prefix 前缀
     * @param ip IP地址
     * @return 缓存键
     */
    public static String createCacheKey(String prefix, String ip) {
        if (!StringUtils.hasText(prefix) || !StringUtils.hasText(ip)) {
            throw new IllegalArgumentException("Prefix and IP cannot be null or empty");
        }
        
        return prefix + ":" + ip.replace(":", "_");
    }

    /**
     * 创建统计缓存键
     * 
     * @param prefix 前缀
     * @param ip IP地址
     * @param date 日期
     * @return 统计缓存键
     */
    public static String createStatsCacheKey(String prefix, String ip, String date) {
        return String.format("%s:stats:%s:%s", prefix, date, ip.replace(":", "_"));
    }

    /**
     * 生成IP范围
     * 
     * @param startIp 起始IP
     * @param endIp 结束IP
     * @return IP范围列表
     */
    public static List<String> generateIPRange(String startIp, String endIp) {
        List<String> result = new ArrayList<>();
        
        if (!IpValidationUtil.isValidIPv4(startIp) || !IpValidationUtil.isValidIPv4(endIp)) {
            return result;
        }
        
        try {
            long startLong = IpValidationUtil.ipv4ToLong(startIp);
            long endLong = IpValidationUtil.ipv4ToLong(endIp);
            
            if (startLong > endLong) {
                return result;
            }
            
            // 限制生成的IP数量，避免内存溢出
            long maxCount = 1000;
            long count = Math.min(endLong - startLong + 1, maxCount);
            
            for (long i = 0; i < count; i++) {
                result.add(IpValidationUtil.longToIPv4(startLong + i));
            }
            
        } catch (Exception e) {
            // 忽略错误，返回空列表
        }
        
        return result;
    }

    /**
     * 计算两个IP之间的距离（仅限IPv4）
     * 
     * @param ip1 第一个IP
     * @param ip2 第二个IP
     * @return IP之间的距离
     */
    public static long calculateIPDistance(String ip1, String ip2) {
        if (!IpValidationUtil.isValidIPv4(ip1) || !IpValidationUtil.isValidIPv4(ip2)) {
            return -1;
        }
        
        try {
            long long1 = IpValidationUtil.ipv4ToLong(ip1);
            long long2 = IpValidationUtil.ipv4ToLong(ip2);
            return Math.abs(long1 - long2);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取IP的地理位置标签
     * 
     * @param ipLocation IP位置信息
     * @return 地理位置标签列表
     */
    public static List<String> getLocationTags(IPLocation ipLocation) {
        List<String> tags = new ArrayList<>();
        
        if (ipLocation == null) {
            return tags;
        }
        
        if (StringUtils.hasText(ipLocation.getCountry()) && !"未知".equals(ipLocation.getCountry())) {
            tags.add(ipLocation.getCountry());
        }
        
        if (StringUtils.hasText(ipLocation.getRegion()) && !"未知".equals(ipLocation.getRegion()) &&
            !Objects.equals(ipLocation.getRegion(), ipLocation.getCountry())) {
            tags.add(ipLocation.getRegion());
        }
        
        if (StringUtils.hasText(ipLocation.getCity()) && !"未知".equals(ipLocation.getCity()) &&
            !Objects.equals(ipLocation.getCity(), ipLocation.getRegion())) {
            tags.add(ipLocation.getCity());
        }
        
        if (StringUtils.hasText(ipLocation.getIsp()) && !"未知".equals(ipLocation.getIsp())) {
            tags.add(ipLocation.getIsp());
        }
        
        return tags;
    }

    /**
     * 生成IP访问签名
     * 
     * @param ip IP地址
     * @param userAgent 用户代理
     * @param timestamp 时间戳
     * @return 访问签名
     */
    public static String generateAccessSignature(String ip, String userAgent, long timestamp) {
        String combined = ip + "|" + userAgent + "|" + timestamp;
        return Integer.toHexString(combined.hashCode());
    }

    /**
     * 检查IP访问频率是否异常
     * 
     * @param accessCount 访问次数
     * @param timeWindowMinutes 时间窗口（分钟）
     * @return 是否异常
     */
    public static boolean isAbnormalAccessFrequency(long accessCount, long timeWindowMinutes) {
        if (timeWindowMinutes <= 0) {
            return false;
        }
        
        // 计算每分钟平均访问次数
        double avgPerMinute = (double) accessCount / timeWindowMinutes;
        
        // 设定阈值：每分钟超过100次访问视为异常
        return avgPerMinute > 100;
    }

    /**
     * 格式化时间戳
     * 
     * @param timestamp 时间戳
     * @return 格式化后的时间字符串
     */
    public static String formatTimestamp(long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, 
                java.time.ZoneOffset.ofHours(8))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 判断IP是否可能是爬虫
     * 
     * @param userAgent 用户代理
     * @param accessPattern 访问模式
     * @return 是否可能是爬虫
     */
    public static boolean isPossibleBot(String userAgent, Map<String, Object> accessPattern) {
        if (!StringUtils.hasText(userAgent)) {
            return true; // 没有User-Agent通常是爬虫
        }
        
        String lowerUserAgent = userAgent.toLowerCase();
        
        // 常见爬虫标识
        String[] botKeywords = {
            "bot", "spider", "crawler", "scraper", "fetcher",
            "googlebot", "bingbot", "baiduspider", "yandexbot",
            "facebookexternalhit", "twitterbot", "linkedinbot"
        };
        
        for (String keyword : botKeywords) {
            if (lowerUserAgent.contains(keyword)) {
                return true;
            }
        }
        
        // 检查访问模式
        if (accessPattern != null) {
            // 访问频率过高
            Object frequency = accessPattern.get("frequency");
            if (frequency instanceof Number && ((Number) frequency).doubleValue() > 1000) {
                return true;
            }
            
            // 用户代理过于简单
            if (userAgent.length() < 50) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取IP的风险评分
     * 
     * @param ipInfo IP信息
     * @param accessCount 访问次数
     * @param errorRate 错误率
     * @return 风险评分（0-100）
     */
    public static int calculateRiskScore(IPInfo ipInfo, long accessCount, double errorRate) {
        int score = 0;
        
        // 基于访问次数
        if (accessCount > 10000) {
            score += 40;
        } else if (accessCount > 1000) {
            score += 20;
        } else if (accessCount > 100) {
            score += 10;
        }
        
        // 基于错误率
        if (errorRate > 50) {
            score += 30;
        } else if (errorRate > 20) {
            score += 15;
        } else if (errorRate > 10) {
            score += 5;
        }
        
        // 基于IP类型
        if (ipInfo != null) {
            if (Boolean.TRUE.equals(ipInfo.getIsProxy())) {
                score += 20;
            }
            
            if (Boolean.TRUE.equals(ipInfo.getIsPrivate())) {
                score -= 10; // 内网IP风险较低
            }
            
            if (ipInfo.getSecurityLevel() != null) {
                switch (ipInfo.getSecurityLevel()) {
                    case 3: // 危险
                        score += 30;
                        break;
                    case 2: // 可疑
                        score += 15;
                        break;
                    case 1: // 安全
                        score -= 5;
                        break;
                }
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }
}